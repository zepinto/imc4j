package pt.lsts.mvplanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.PlanManeuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.SetEntityParameters;
import pt.lsts.imc4j.msg.TemporalPlan;
import pt.lsts.imc4j.msg.VehicleDepot;
import pt.lsts.imc4j.util.FormatConversion;

public class PddlParser {

	public static String initialState(PddlPlan plan, int vehicle) {

		ArrayList<PddlLocation> locations = plan.locations(vehicle);

		// start printing...
		StringBuilder sb = new StringBuilder();
		sb.append("(define (problem LSTSprob)(:domain LSTS)\n(:objects\n ");

		// print location names
		for (PddlLocation loc : locations) {
			sb.append(" " + loc.name);
		}
		sb.append(" - location\n  ");

		// print vehicle names
		sb.append(VehicleParams.vehicleNickname(vehicle) + " - auv\n  ");

		// print payload names
		for (PayloadRequirement pr : VehicleParams.payloadsFor(vehicle))
			sb.append(VehicleParams.vehicleNickname(vehicle) + "_" + pr.name() + " - " + pr.name() + "\n  ");

		ArrayList<IPddlAction> allActions = new ArrayList<>();

		// Names of survey tasks' areas
		for (IPddlAction action : plan.surveyActions(vehicle)) {
			allActions.add(action);
			sb.append(action.getAssociatedTask() + "_area ");
		}

		if (plan.surveyActions(vehicle).isEmpty())
			sb.append("dummy_area ");

		sb.append("- area\n  ");

		// Names of sample tasks' objects of interest
		for (IPddlAction action : plan.sampleActions(vehicle)) {
			allActions.add(action);
			sb.append(action.getAssociatedTask() + "_obj ");
		}

		if (plan.sampleActions(vehicle).isEmpty())
			sb.append("dummy_obj ");

		sb.append("- oi\n  ");

		// Task names
		for (IPddlAction action : allActions) {

			for (PayloadRequirement payload : payloads(action)) {
				sb.append(action.getAssociatedTask() + "_" + payload.name() + " ");
			}

		}
		sb.append("- task\n");

		sb.append(")\n(:init\n");

		// distance between all locations
		sb.append(distances(locations));

		sb.append("  (= (tasks-completed) 0)\n");

		sb.append("  (= (base-returns) 0) ; \"cost\" of returning to the depots\n\n");

		// details of vehicle
		sb.append(vehicleDetails(vehicle, plan));

		// survey tasks
		sb.append(surveyTasks(vehicle, plan));

		// sample tasks
		sb.append(sampleTasks(vehicle, plan));
		sb.append(")\n");

		// goals to solve
		sb.append(goals(vehicle, plan));

		return sb.toString();
	}

	private static String distances(ArrayList<PddlLocation> locations) {

		StringBuilder sb = new StringBuilder();

		// distance between all locations
		for (int i = 0; i < locations.size(); i++) {
			for (int j = 0; j < locations.size(); j++) {
				if (i == j)
					continue;

				PddlLocation loc1 = locations.get(i);
				PddlLocation loc2 = locations.get(j);
				double dist = Math.max(0.01, loc1.distanceTo(loc2));
				dist /= CommonSettings.SPEED;
				sb.append("  (=(distance " + loc1.name + " " + loc2.name + ") " + String.format(Locale.US, "%.2f", dist)
						+ ")\n");
			}
		}
		sb.append("\n");
		return sb.toString();
	}

	protected static String vehicleDetails(int v, PddlPlan plan) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n;" + VehicleParams.vehicleNickname(v) + ":\n");
		sb.append("  (can-move " + VehicleParams.vehicleNickname(v) + ")\n");
		sb.append("  (= (speed " + VehicleParams.vehicleNickname(v) + ") " + CommonSettings.SPEED + ")\n");
		sb.append(
				"  (base " + VehicleParams.vehicleNickname(v) + " " + VehicleParams.vehicleNickname(v) + "_depot)\n\n");
		sb.append("  (at " + VehicleParams.vehicleNickname(v) + " " + VehicleParams.vehicleNickname(v) + "_depot"
				+ ")\n");
		for (PayloadRequirement req : VehicleParams.payloadsFor(v))
			sb.append("  (having " + VehicleParams.vehicleNickname(v) + "_" + req.name() + " "
					+ VehicleParams.vehicleNickname(v) + ")\n");
		
		double secsLeft = (plan.deadlines.get(v).getTime() - System.currentTimeMillis()) / 1000.0;
		int timeLeft = (int) secsLeft;
		sb.append("  (= (time-elapsed " + VehicleParams.vehicleNickname(v)
				+ ") 0) ; how long the vehicle is \"operating\"\n");
		sb.append("  (= (goal-time " + VehicleParams.vehicleNickname(v) + ") " + timeLeft
				+ ") ; maximum time for the vehicle to be outside its depot\n");

		return sb.toString();
	}

	private static HashSet<PayloadRequirement> payloads(IPddlAction action) {

		HashSet<PayloadRequirement> reqs = new HashSet<>();
		PlanSpecification spec = action.getBehavior();

		for (PlanManeuver man : spec.maneuvers) {
			for (Message msg : man.start_actions) {
				if (msg.mgid() == SetEntityParameters.ID_STATIC) {
					SetEntityParameters params = (SetEntityParameters) msg;
					try {
						reqs.add(PayloadRequirement.valueOf(params.name.toLowerCase()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return reqs;
	}

	private static String surveyTasks(int vehicle, PddlPlan plan) {
		ArrayList<SurveyAction> actions = plan.surveyActions(vehicle);

		StringBuilder sb = new StringBuilder();

		for (SurveyAction t : actions) {
			sb.append("\n;" + t.getAssociatedTask() + " survey:\n");
			sb.append("  (available " + t.getAssociatedTask() + "_area)\n");
			sb.append("  (free " + t.getAssociatedTask() + "_entry" + ")\n");
			sb.append("  (free " + t.getAssociatedTask() + "_exit" + ")\n");
			sb.append("  (entry " + t.getAssociatedTask() + "_area " + t.getAssociatedTask() + "_entry" + ")\n");
			sb.append("  (exit " + t.getAssociatedTask() + "_area " + t.getAssociatedTask() + "_exit" + ")\n");

			sb.append("  (=(surveillance_distance " + t.getAssociatedTask() + "_area) "
					+ String.format(Locale.US, "%.2f", t.getDuration()) + ")\n");

			for (PayloadRequirement r : payloads(t)) {
				sb.append("  (task_desc " + t.getAssociatedTask() + "_" + r.name() + " " + t.getAssociatedTask()
						+ "_area " + VehicleParams.vehicleNickname(vehicle) + "_" + r.name() + ")\n");
			}
		}
		return sb.toString();
	}

	private static String sampleTasks(int vehicle, PddlPlan plan) {
		StringBuilder sb = new StringBuilder();

		ArrayList<SampleAction> actions = plan.sampleActions(vehicle);

		for (SampleAction t : actions) {
			sb.append("\n  ;" + t.getAssociatedTask() + " object of interest:\n");
			sb.append("  (free " + t.getAssociatedTask() + "_oi)\n");
			sb.append("  (at_oi " + t.getAssociatedTask() + "_obj " + t.getAssociatedTask() + "_oi" + ")\n");

			for (PayloadRequirement r : payloads(t)) {
				sb.append("  (task_desc " + t.getAssociatedTask() + "_" + r.name() + " " + t.getAssociatedTask()
						+ "_obj " + VehicleParams.vehicleNickname(vehicle) + "_" + r.name() + ")\n");

			}
		}
		sb.append("\n");

		return sb.toString();
	}

	private static String goals(int vehicle, PddlPlan plan) {
		StringBuilder sb = new StringBuilder();
		sb.append("(:goal (and\n");
		String nick = VehicleParams.vehicleNickname(vehicle);
		sb.append("  (at " + nick + " " + nick + "_depot)\n");
		sb.append("  (>= (tasks-completed) 1)\n");
		sb.append("))\n");
		sb.append("(:metric maximize (tasks-completed)))\n");
		return sb.toString();
	}

	private static IPddlAction[] createAction(PddlPlan plan, int vehicle, String line) throws Exception {
		line = line.trim();
		String regex = "[\\:\\(\\)\\[\\] ]+";
		String[] parts = line.split(regex);
		AbstractPddlAction action = null;
		String actionStr, actionType;
		double start, end;
		String name;
		ArrayList<IPddlAction> actions = plan.actions(vehicle);
		LinkedHashMap<String, PddlLocation> locations = new LinkedHashMap<>();
		for (PddlLocation loc : plan.locations(vehicle))
			locations.put(loc.name, loc);
		
		try {
			actionStr = parts[1];
			actionType = actionStr;
			if (actionStr.contains("-"))
				actionType = actionStr.substring(0, actionStr.indexOf('-'));

			if (actionType.equals("getready"))
				return null;
			
			
			
			start = Double.parseDouble(parts[0]) + System.currentTimeMillis()/1000.0;
			
			end = (long) Double.parseDouble(parts[parts.length - 1]) + start;
			
			name = parts[3];
			name = name.replaceAll("_entry", "");
			name = name.replaceAll("_exit", "");
			name = name.replaceAll("_oi", "");
			name = name.replaceAll("_depot", "");

		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Unrecognized PDDL syntax on line '" + line + "'", e);
		}

		for (IPddlAction act : actions) {
			if (act.getAssociatedTask().equals(name))
				action = (AbstractPddlAction) act;
		}
		
		
		switch (actionType) {
		case "move":
			action = new MoveAction(locations.get(parts[4]), new Date((long)(start*1000)), new Date((long)(end*1000)));
			break;
		case "surface":
			action = new SurfaceAction(locations.get(parts[4]), new Date((long)(start*1000)), new Date((long)(end*1000)));
			break;
		case "locate":
			action = new LocateAction(locations.get(parts[4]), new Date((long)(start*1000)), new Date((long)(end*1000)));
			break;
		case "communicate":
			action = new CommunicateAction(locations.get(parts[4]), new Date((long)(start*1000)), new Date((long)(end*1000)));
			break;
		case "survey":
		case "sample":
			action.setStartTime(new Date((long)(start*1000)));
			action.setEndTime(new Date((long) (end * 1000)));
			break;
		default:
			System.err.println("Did not recognize this action type: "+actionType);
			return null;
		}
		
		return new IPddlAction[] { action };
	}

	public static PddlPlan parseSolution(PddlPlan previousPlan, int vehicle, String solution) throws Exception {
		ArrayList<IPddlAction> newActions = new ArrayList<>();

		for (String line : solution.split("\n")) {
			if (line.trim().isEmpty() || line.trim().startsWith(";"))
				continue;
			IPddlAction[] act = createAction(previousPlan, vehicle, line.toLowerCase());
			if (act != null) {
				for (IPddlAction a : act)
					newActions.add(a);
			}
		}
		PddlPlan newPlan = new PddlPlan();
		newPlan.deadlines.putAll(previousPlan.deadlines);
		newPlan.depots.putAll(previousPlan.depots);
		newPlan.actions.addAll(newActions);

		return newPlan;
	}

	public static String replan(PddlPlan plan, int vehicle, int secs) throws Exception {
		String cmd = "lpg -o DOMAIN -f INITIAL_STATE -out OUTFILE -n 10 -cputime " + secs;

		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timestamp = fmt.format(new Date());
		File input_file = new File("log/pddl/" + timestamp + "_problem.pddl");
		File output_file = new File("log/pddl/" + timestamp + ".SOL");
		File domain_file = new File("log/pddl/" + timestamp + "_domain.pddl");

		System.out.println("Writing domain model to " + domain_file.getPath());

		domain_file.getParentFile().mkdirs();
		Files.copy(PddlParser.class.getClassLoader().getResourceAsStream("pt/lsts/mvplanner/domain.pddl"),
				domain_file.toPath(), StandardCopyOption.REPLACE_EXISTING);

		System.out.println("Writing initial state to " + input_file.getPath());
		String initial_state = initialState(plan, vehicle);
		Files.write(input_file.toPath(), initial_state.getBytes());
		System.out.println(initial_state);

		cmd = cmd.replaceAll("DOMAIN", domain_file.getAbsolutePath());
		cmd = cmd.replaceAll("INITIAL_STATE", input_file.getAbsolutePath());
		cmd = cmd.replaceAll("OUTFILE", output_file.getAbsolutePath());

		cmd = cmd.replaceAll("/", System.getProperty("file.separator"));
		System.out.println("Executing " + cmd + "...");
		Process p = Runtime.getRuntime().exec(cmd, null, new File("log/pddl"));

		Thread monitor = new Thread() {
			public void run() {
				try {
					if (!p.waitFor(secs + 1, TimeUnit.SECONDS)) {
						System.out.println("Killing LPG process.");
						p.destroyForcibly();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		monitor.setDaemon(true);
		monitor.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = reader.readLine();

		while (line != null) {
			System.out.println(line);
			line = reader.readLine();
		}
		if (output_file.canRead()) {
			return new String(Files.readAllBytes(output_file.toPath()));
		} else {
			System.out.println("No solution was produced.");
			return null;
		}

	}

	public static void main(String[] args) throws Exception {
		String json = new String(Files.readAllBytes(new File("res/temporalplan.json").toPath()));
		TemporalPlan plan = (TemporalPlan) FormatConversion.fromJson(json);
		for (VehicleDepot depot : plan.depots)
			depot.deadline = new Date().getTime() / 1000.0 + 1830;

		PddlPlan pplan = PddlPlan.parse(plan);
		
		while (true) {
			String solution = PddlParser.replan(pplan, 27, 3);
			pplan = PddlParser.parseSolution(pplan, 27, solution);
			Thread.sleep(30000);
		}		
	}
}
