package pt.lsts.autonomy;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.*;
import pt.lsts.imc4j.util.FormatConversion;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.mvplanner.PddlParser;
import pt.lsts.mvplanner.PddlPlan;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;

/**
 * Created by tsm on 31/05/17.
 * Added onboard replanning by ZP 10/07/17.
 */
public class MvReplanningExecutive extends MissionExecutive {
    
	@Parameter
    public String host = "127.0.0.1";

    @Parameter
    public int port = 6003;

    @Parameter
    public int systemId = -1;
    
    @Parameter
    public int replanningSecs = 10;
    

    /** Current plan being executed **/
    private TemporalPlan currPlan = null;

    /** Actions for this system to execute  **/
    private final Queue<TemporalAction> toExecute = new PriorityQueue<>(Comparator.comparingDouble(o -> o.start_time));

    /** All finished or ongoing tasks **/
    private final Stack<TemporalAction> handledActions = new Stack<>();

    /** Current being executed  **/
    private TemporalAction currAction = null;

    private TemporalPlanStatus status = null;

    public MvReplanningExecutive() throws ParseException {
        this.state = this::init;
    }

    @Consume
    public void on(TemporalPlan msg) {
        synchronized (status) {
            status = new TemporalPlanStatus();
        }

        currPlan = msg;

        print("Got a new plan with " + currPlan.actions.size() + " actions");

        toExecute.clear();
        currPlan.actions.stream()
                .filter(a -> a.system_id == systemId)
                .forEach(a -> toExecute.add(a));

        print("To execute: " + toExecute.size() + " actions");
    }

    private State init() {
        print("init");
        if(systemId == -1) {
            Announce msg = get(Announce.class);

            if(msg == null || msg.sys_type != SystemType.UUV) {
                print("Waiting for host id");
                return this::init;
            }

            systemId = msg.src;
            print("Running in " + msg.sys_name + " with id " + systemId);
            dispatch(new TemporalPlanStatus());
        }

        if(currPlan == null) {
            print("Waiting for a temporal plan...");
            return this::init;
        }

        return this::idle;
    }

    /**
     * Wait for TemporalPlan and then,
     * for appropriate time to run a TemporalAction
     * */
    private State idle() {
        print("idle ");
        if(currPlan == null || toExecute.isEmpty())
            return this::idle;

        long currTime = System.currentTimeMillis();
        double nextActionTime = toExecute.peek().start_time;
        print("Next action in: " + ((nextActionTime - currTime) / 1000) + "s");

        if(currTime >=  nextActionTime)
            return this::exec;

        return this::idle;
    }

    /**
     * Execute a TemporalAction
     * */
    private State exec() {
        print("exec");

        if(toExecute.isEmpty())
            return this::idle;

        currAction = toExecute.peek();

        PlanControl pc = new PlanControl();
        pc.plan_id = currAction.action_id;
        pc.arg = currAction.action;
        pc.op = PlanControl.OP.PC_START;
        pc.type = PlanControl.TYPE.PC_REQUEST;

        try {
            send(pc);
        } catch (IOException e) {
            e.printStackTrace();

            currAction = null;
            print("Failed to send " + currAction.action.plan_id + " trying again...");
            return this::exec;
        }

        print("Executing action with id " + currAction.action.plan_id);

        toExecute.poll();
        TemporalAction action = new TemporalAction();
        action.action_id = currAction.action_id;
        action.system_id = currAction.system_id;
        action.status = TemporalAction.STATUS.ASTAT_SCHEDULED;

        handledActions.push(action);

        return this::monitor;
    }

    protected State monitor() {
        PlanControlState msg = get(PlanControlState.class);
        if(msg == null || msg.src != currAction.system_id  || !msg.plan_id.contains(currAction.action_id))
            return this::monitor;

        boolean success = false;
        boolean failed = false;
        
        if (msg.state == PlanControlState.STATE.PCS_EXECUTING && msg.plan_eta > 0 && msg.plan_eta <= replanningSecs) {
        	if (!replanning)
        		replan();
        }
        if (msg.state == PlanControlState.STATE.PCS_READY || msg.state == PlanControlState.STATE.PCS_BLOCKED) {
            success = msg.last_outcome == PlanControlState.LAST_OUTCOME.LPO_SUCCESS;
            failed = !success;
        }
        else if (msg.state == PlanControlState.STATE.PCS_READY && msg.plan_progress == 100)
            success = true;

        if(success) {
            print("Finished " + currAction.action_id + " with success");
            handledActions.peek().status = TemporalAction.STATUS.ASTAT_FINISHED;
            currAction = null;

            return this::idle;
        }
        else if(failed) {
            print("Failed " + currAction.action_id);
            handledActions.peek().status = TemporalAction.STATUS.ASTAT_FAILED;
            currAction = null;
            replan();
            return this::idle;
        }

        return this::monitor;
    }
    
    private boolean replanning = false;
    
    protected void replan() {
    	System.out.println(currPlan);
    	PddlPlan plan = PddlPlan.parse(currPlan);
    	
    	print("Replanning for "+replanningSecs+" seconds...");
    	
    	replanning = true;
    	Thread t = new Thread("Replanning") {
    		@Override
    		public void run() {
    			try {    				
    				String solution = PddlParser.replan(plan, remoteSrc, replanningSecs);
    				PddlPlan newPlan = PddlParser.parseSolution(plan, remoteSrc, solution);
    				print("Finished replanning.");
    				replanning = false;
    				on(newPlan.asPlan());            		
    			}
    			catch (Exception e) {
    				e.printStackTrace();
    			}
    			super.run();
    		}    		
    	};
    	t.setDaemon(true);
    	t.start();    	
    }
    
    protected State replanning() {
    	if (replanning)
    		return this::replanning;
    	else
    		return this::idle;    	
    }

    @Periodic(10000)
    private void communicate() {
        print("Communicating");

        if(status == null)
            status = new TemporalPlanStatus();

        synchronized (status) {
            if (currPlan != null) {
                status.plan_id = currPlan.plan_id;
                status.actions = new ArrayList<>(handledActions);
            }

            try {
            	sendOut(status);
            } catch (IOException e) {
                print("Failed to communicate...");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        MvReplanningExecutive exec;

        if(args.length > 0 && args[0].equals("--test")) {
            System.out.println("Testing....");
            String[] subset = Arrays.copyOfRange(args, 1, args.length);
            exec = PojoConfig.create(MvReplanningExecutive.class, subset);
            exec.connect(exec.host, exec.port);

            File f = new File("/home/tsm/data.json");
            try {
                List<String> content = Files.readAllLines(f.toPath(), Charset.defaultCharset());
                StringBuilder sb = new StringBuilder();

                content.forEach(l -> sb.append(l + "\n"));

                Thread.sleep(10000);
                System.out.println("Sending temporal plan");
                exec.on((TemporalPlan) FormatConversion.fromJson(sb.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        else {
        exec = PojoConfig.create(MvReplanningExecutive.class, args);
        exec.connect(exec.host, exec.port);
        }

        exec.join();
    }
}
