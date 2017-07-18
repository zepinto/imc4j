package pt.lsts.autonomy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.PlanControl;
import pt.lsts.imc4j.msg.PlanControl.OP;
import pt.lsts.imc4j.msg.PlanControlState;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.StationKeeping;
import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalAction.TYPE;
import pt.lsts.imc4j.msg.TemporalPlan;
import pt.lsts.imc4j.msg.TemporalPlanStatus;
import pt.lsts.imc4j.util.FormatConversion;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;
import pt.lsts.mvplanner.CommonSettings;
import pt.lsts.mvplanner.PddlParser;
import pt.lsts.mvplanner.PddlPlan;

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
	
	@Parameter
	public int replanAheadOfTaskTermination = 0;
	
	@Parameter
	public boolean sendStatusOnlyOnCommunicate = true;
	
	@Parameter
	public int forcedFailureSeconds = 300;
	

	/** Current plan being executed **/
	private TemporalPlan currPlan = null;

	/** Actions for this system to execute  **/
	private final Queue<TemporalAction> toExecute = new PriorityQueue<>(Comparator.comparingDouble(o -> o.start_time));

	/** All finished or ongoing tasks **/
	private final Stack<TemporalAction> handledActions = new Stack<>();

	/** Current being executed  **/
	private TemporalAction currAction = null;

	private TemporalPlanStatus status = null;
	
	private int countFailure = 0;

	public MvReplanningExecutive() throws ParseException {
		this.state = this::init;
	}

	@Consume
	public void on(TemporalPlan msg) {
		synchronized (status) {
			status = new TemporalPlanStatus();
		}

		currPlan = msg;

		print("Got a new plan with " + currPlan.actions.size() + " actions from "+ msg.src);
		System.out.println(msg);		

		toExecute.clear();
		
		currPlan.actions.stream()
		.filter(a -> a.system_id == systemId && a.status != TemporalAction.STATUS.ASTAT_CANCELLED)
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
		print("Next action in ("+nextActionTime+") ("+currTime+"): " + ((nextActionTime - currTime/1000) ) + "s");

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

		print("Executing action with id " + currAction.action.plan_id+" of type "+currAction.type);

		toExecute.poll();
		TemporalAction action = new TemporalAction();
		action.action_id = currAction.action_id;
		action.system_id = currAction.system_id;
		action.start_time = currAction.start_time;
		action.duration = currAction.duration;
		action.type = currAction.type;
		action.status = TemporalAction.STATUS.ASTAT_SCHEDULED;

		handledActions.push(action);

		return this::monitor;
	}

	protected State monitor() {
		
		countFailure++;
		if (countFailure > forcedFailureSeconds && forcedFailureSeconds > 0) {
			print("Simulated failure!");
			stopPlan();
		}
		
		PlanControlState msg = get(PlanControlState.class);
		if (currAction == null)
			return this::monitor;
		if(msg == null || msg.src != currAction.system_id  || !msg.plan_id.contains(currAction.action_id))
			return this::monitor;

		boolean success = false;
		boolean failed = false;

		if (replanAheadOfTaskTermination > 0 && msg.state == PlanControlState.STATE.PCS_EXECUTING && msg.plan_eta > 0 && msg.plan_eta <= replanAheadOfTaskTermination) { 
			if (!replanning)
				replan(true);
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
			replan(false);
			return this::replanning;
		}

		return this::monitor;
	}
	
	protected void startSkeeping(int secs) {
		StationKeeping sk = new StationKeeping();
		sk.duration = secs;
		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		sk.lat = Math.toRadians(pos[0]);
		sk.lon = Math.toRadians(pos[1]);
		sk.speed = CommonSettings.SPEED;
		sk.speed_units = CommonSettings.SPEED_UNITS;
		sk.z = 0;
		sk.z_units = ZUnits.DEPTH;
		sk.radius = 20;
	
		PlanSpecification spec = spec(sk);
		spec.plan_id = "replanning";
		PlanControl pc = new PlanControl();
		pc.arg = spec;
		pc.plan_id = spec.plan_id;
		pc.op = OP.PC_START;
		pc.type = PlanControl.TYPE.PC_REQUEST;
		pc.dst = remoteSrc;
		try {
			send(pc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean replanning = false;

	protected void replan(boolean skipCurrentAction) {
		TemporalPlan copy = null;
		try {
			copy = (TemporalPlan) FormatConversion.fromJson(FormatConversion.asJson(currPlan));
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		if (skipCurrentAction) {
			int index = -1;
			for (int i = 0; i < copy.actions.size(); i++) {
				if (copy.actions.get(i).action_id.equals(currAction.action_id)) {
					index = i;
					break;
				}
			}
			// ignore current action
			if (index != -1)
				copy.actions.remove(index);	
		}		
		
		
		
		PddlPlan plan = PddlPlan.parse(currPlan);
		
		print("Replanning for "+replanningSecs+" seconds...");
		startSkeeping(replanningSecs);
		replanning = true;
		Thread t = new Thread("Replanning") {
			@Override
			public void run() {
				try {    				
					EstimatedState state = get(EstimatedState.class);
					double[] position = WGS84Utilities.toLatLonDepth(state);
					
					String solution = PddlParser.replan(plan, position, remoteSrc, replanningSecs);
					print("Finished replanning.");
					System.out.println(solution);
					replanning = false;
					PddlPlan newPlan = PddlParser.parseSolution(plan, remoteSrc, solution);
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
			return this::exec;    	
	}

	@Periodic(10000)
	private void communicate() {
		if (sendStatusOnlyOnCommunicate && toExecute.size()>1)
			return;
		
		if (sendStatusOnlyOnCommunicate && (currAction != null && currAction.type != TYPE.ATYPE_COMMUNICATE))
			return;
		
		if (replanning)
			return;
		
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
				broadcast(status);
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

			System.out.println("MVReplanner started with settings:");
			for (Field f : exec.getClass().getDeclaredFields()) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					System.out.println(f.getName() + "=" + f.get(exec));
				}
			}
			System.out.println();

			exec.connect(exec.host, exec.port);
		}

		exec.join();
	}
}
