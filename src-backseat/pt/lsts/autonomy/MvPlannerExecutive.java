package pt.lsts.autonomy;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.*;
import pt.lsts.imc4j.util.FormatConversion;
import pt.lsts.imc4j.util.PojoConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;

/**
 * Created by tsm on 31/05/17.
 */
public class MvPlannerExecutive extends MissionExecutive {
    @Parameter
    public String host = "127.0.0.1";

    @Parameter
    public int port = 6003;

    @Parameter
    public int systemId = -1;

    /** Current plan being executed **/
    private TemporalPlan currPlan = null;

    /** Actions for this system to execute  **/
    private final Queue<TemporalAction> toExecute = new PriorityQueue<>(Comparator.comparingDouble(o -> o.start_time));

    /** All finished or ongoing tasks **/
    private final Stack<TemporalAction> handledActions = new Stack<>();

    /** Current being executed  **/
    private TemporalAction currAction = null;

    public MvPlannerExecutive() throws ParseException {
        this.state = this::init;
    }

    @Consume
    public void on(TemporalPlan msg) {
        currPlan = msg;

        log("Got a new plan with " + currPlan.actions.size() + " actions");

        toExecute.clear();
        currPlan.actions.stream()
                .filter(a -> a.system_id == systemId)
                .forEach(a -> toExecute.add(a));

        log("To execute: " + toExecute.size() + " actions");
    }

    private State init() {
        log("init");
        if(systemId == -1) {
            Announce msg = get(Announce.class);

            if(msg == null || msg.sys_type != SystemType.UUV) {
		log("Waiting for host id");
		return this::init;
	    }

            systemId = msg.src;
            log("Running in " + msg.sys_name + " with id " + systemId);
        }

        if(currPlan == null) {
	    log("Waiting for a temporal plan...");
	    return this::init;
	}

        return this::idle;
    }

    /**
     * Wait for TemporalPlan and then,
     * for appropriate time to run a TemporalAction
     * */
    private State idle() {
        log("idle ");
        if(currPlan == null)
            return this::idle;

        if(!handledActions.isEmpty()) {
            Announce msg = get(Announce.class);
            if(msg != null && msg.sys_type != SystemType.CCU)
                return this::communicate;
        }

        long currTime = System.currentTimeMillis();
        double nextActionTime = currPlan.actions.get(0).start_time;
        log("Next action in: " + ((nextActionTime - currTime) / 1000) + "s");
        if(currTime >=  nextActionTime)
            return this::exec;

        return this::idle;
    }

    /**
     * Execute a TemporalAction
     * */
    private State exec() {
        log("exec");
        // allocate task
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
            log("Failed to send " + currAction.action.plan_id + " trying again...");
            return this::exec;
        }

        log("Executing action with id " + currAction.action.plan_id);
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
        if (msg.state == PlanControlState.STATE.PCS_READY || msg.state == PlanControlState.STATE.PCS_BLOCKED) {
            success = msg.last_outcome == PlanControlState.LAST_OUTCOME.LPO_SUCCESS;
            failed = !success;
        }
        else if (msg.state == PlanControlState.STATE.PCS_READY && msg.plan_progress == 100)
            success = true;

        if(success) {
            log("Finished " + currAction.action_id + " with success");
            handledActions.peek().status = TemporalAction.STATUS.ASTAT_FINISHED;
            currAction = null;

            return this::idle;
        }
        else if(failed) {
            log("Failed " + currAction.action_id);
            handledActions.peek().status = TemporalAction.STATUS.ASTAT_FAILED;
            currAction = null;

            return this::idle;
        }

        return this::monitor;
    }

    /**
     * Communicate with a Neptus console the current status
     * of the TemporalPlan
     * */
    private synchronized State communicate() {
        log("Communicating");
        handledActions.forEach(a -> dispatch(a));
        handledActions.clear();

        return this::idle;
    }

    private void log(String msg) {
        print("[MvPlannerExecutive]: " + msg);
    }

    public static void main(String[] args) throws Exception {
        MvPlannerExecutive exec;

        if(args.length > 0 && args[0].equals("--test")) {
            System.out.println("Testing....");
            String[] subset = Arrays.copyOfRange(args, 1, args.length);
            exec = PojoConfig.create(MvPlannerExecutive.class, subset);
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
            exec = PojoConfig.create(MvPlannerExecutive.class, args);
            exec.connect(exec.host, exec.port);
        }

        exec.join();
    }
}
