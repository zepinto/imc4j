package pt.lsts.mvplanner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.PlanManeuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.SetEntityParameters;
import pt.lsts.imc4j.msg.TemporalPlan;
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

		sb.append(" - oi\n  ");

		// Task names
		for (IPddlAction action : allActions) {
			sb.append(action.getAssociatedTask() + " ");
		}
		sb.append("- task\n");

		sb.append(")\n(:init\n");

		// distance between all locations
		sb.append(distances(locations));

		sb.append("\n  (= (tasks-completed) 0)\n\n");

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
		double moveConsumption = VehicleParams.moveConsumption(v) * 1000 / 3600.0;
		sb.append("  (= (speed " + VehicleParams.vehicleNickname(v) + ") " + CommonSettings.SPEED + ")\n");
		sb.append("  (= (battery-consumption-move " + VehicleParams.vehicleNickname(v) + ") "
				+ String.format(Locale.US, "%.2f", moveConsumption) + ")\n");
		sb.append("  (= (battery-level " + VehicleParams.vehicleNickname(v) + ") " + VehicleParams.maxBattery(v) * 1000
				+ ")\n");
		sb.append(
				"  (base " + VehicleParams.vehicleNickname(v) + " " + VehicleParams.vehicleNickname(v) + "_depot)\n\n");
		sb.append("  (at " + VehicleParams.vehicleNickname(v) + " " + VehicleParams.vehicleNickname(v) + "_depot"
				+ ")\n");
		for (PayloadRequirement req : VehicleParams.payloadsFor(v)) {
			double consumption = req.getConsumptionPerHour() / 3600.0 * 1000;
			sb.append("  (= (battery-consumption-payload " + req.name() + ") "
					+ String.format(Locale.US, "%.2f", consumption) + ")\n");
			sb.append("  (having " + req.name() + " " + VehicleParams.vehicleNickname(v) + ")\n");
		}

		int timeLeft = (int) ((System.currentTimeMillis() - plan.deadlines.get(v).getTime()) / 1000);
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
		for (SampleAction t : plan.sampleActions(vehicle)) {
			for (PayloadRequirement r : payloads(t)) {
				sb.append("  (communicated_data " + t.getAssociatedTask() + "_" + r.name() + ")\n");
			}
		}

		for (SurveyAction t : plan.surveyActions(vehicle)) {
			System.out.println(payloads(t));
			for (PayloadRequirement r : payloads(t)) {
				sb.append("  (communicated_data " + t.getAssociatedTask() + "_" + r.name() + ")\n");
			}
		}

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

		try {
			actionStr = parts[1];
			actionType = actionStr;
			if (actionStr.contains("-"))
				actionType = actionStr.substring(0, actionStr.indexOf('-'));

			if (actionType.equals("getready"))
				return null;

			start = Double.parseDouble(parts[0]) + System.currentTimeMillis();
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

		if (action == null) {
			System.err.println("Could not find action named " + name);
			return null;
		}

		action.setStartTime(new Date((long) (start * 1000)));
		action.setEndTime(new Date((long) (end * 1000)));

		return new IPddlAction[] { action };
	}

	public static PddlPlan replan(PddlPlan plan, int vehicle, String solution) throws Exception {
		ArrayList<IPddlAction> newActions = new ArrayList<>();

		for (String line : solution.split("\n")) {
			if (line.trim().isEmpty() || line.trim().startsWith(";"))
				continue;
			IPddlAction[] act = createAction(plan, vehicle, line.toLowerCase());
			if (act != null) {
				for (IPddlAction a : act)
					newActions.add(a);
			}
		}
		PddlPlan newPlan = new PddlPlan();
		newPlan.deadlines.putAll(plan.deadlines);
		newPlan.depots.putAll(plan.depots);
		newPlan.actions.addAll(newActions);

		return newPlan;
	}

	public static void main(String[] args) throws ParseException {

		String json = "{\"abbrev\":\"TemporalPlan\",\"timestamp\":1.499188942933E9,\"src\":0,\"src_ent\":0,\"dst\":65535,\"dst_ent\":255,\"plan_id\":\"plan_2017-07-185_17:22:22\",\"actions\":[{\"abbrev\":\"TemporalAction\",\"action_id\":\"1_move\",\"system_id\":27,\"status\":2,\"start_time\":1.499188942607E9,\"duration\":155,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"1_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7187898315558492,\"lon\":-0.15195677562209114,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"2_surface\",\"system_id\":27,\"status\":2,\"start_time\":1.499189097607E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"2_surface\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7187898315558492,\"lon\":-0.15195677562209114,\"z\":0,\"z_units\":1,\"radius\":20,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":2},{\"abbrev\":\"TemporalAction\",\"action_id\":\"t07\",\"system_id\":27,\"status\":2,\"start_time\":1.499189157608E9,\"duration\":1197.59,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"t07\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"FollowPath\",\"timeout\":10000,\"lat\":0.718795104503189,\"lon\":-0.15198085714401655,\"z\":3,\"z_units\":2,\"speed\":1,\"speed_units\":0,\"points\":[{\"abbrev\":\"PathPoint\",\"x\":-33.702625,\"y\":115.752335,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-55.045094,\"y\":50.0608,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-70.04968,\"y\":-93.21271,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-4.359136,\"y\":108.981,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":24.984352,\"y\":102.20967,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-26.530102,\"y\":-56.350426,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":11.853795,\"y\":-35.295624,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":54.32784,\"y\":95.43833,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":83.671326,\"y\":88.667,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":45.79512,\"y\":-27.91493,\"z\":2}],\"custom\":\"Pattern=AreaSurvey;polygon=41.18357415932586:-8.706467918816196:41.18472351019979:-8.706819034780578:41.18471410387161:-8.706872975867132:41.18439444985875:-8.70817542758394:41.183864727974665:-8.708327867688423:41.18310035594353:-8.70918477174141:41.18310035594353:-8.70918477174141:;angle=1.8849555921538435;corner=A;step=30.0;\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":5},{\"abbrev\":\"TemporalAction\",\"action_id\":\"4_move\",\"system_id\":27,\"status\":2,\"start_time\":1.499190355198E9,\"duration\":182.82,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"4_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"5_move\",\"system_id\":27,\"status\":2,\"start_time\":1.499190538018E9,\"duration\":82.4,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"5_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7188173149470208,\"lon\":-0.15193531834531956,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"6_surface\",\"system_id\":27,\"status\":2,\"start_time\":1.499190620419E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"6_surface\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7188173149470208,\"lon\":-0.15193531834531956,\"z\":0,\"z_units\":1,\"radius\":20,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":2},{\"abbrev\":\"TemporalAction\",\"action_id\":\"t03\",\"system_id\":27,\"status\":2,\"start_time\":1.499190680419E9,\"duration\":892.95,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"t03\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"FollowPath\",\"timeout\":10000,\"lat\":0.7188144995904377,\"lon\":-0.15196368877827382,\"z\":3,\"z_units\":2,\"speed\":1,\"speed_units\":0,\"points\":[{\"abbrev\":\"PathPoint\",\"x\":17.934418,\"y\":136.32036,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-61.02049,\"y\":-106.69018,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-27.079374,\"y\":-99.31088,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":42.657825,\"y\":115.32903,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":67.38123,\"y\":94.33771,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":45.920605,\"y\":28.285341,\"z\":2}],\"custom\":\"Pattern=AreaSurvey;polygon=41.18513176532778:-8.705178137024904:41.185710572502074:-8.705828669856855:41.18569793966793:-8.705880140235122:41.18489166666666:-8.708033333333335:41.184395947843136:-8.708175962829323:41.184395947843136:-8.708175962829323:;angle=1.8849555921538435;corner=A;step=30.0;\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":5},{\"abbrev\":\"TemporalAction\",\"action_id\":\"8_move\",\"system_id\":27,\"status\":2,\"start_time\":1.499191573369E9,\"duration\":57.3,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"8_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"9_move\",\"system_id\":27,\"status\":2,\"start_time\":1.49919163067E9,\"duration\":288.88,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"9_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7187866306205086,\"lon\":-0.15190381316557336,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"10_surface\",\"system_id\":27,\"status\":2,\"start_time\":1.49919191955E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"10_surface\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7187866306205086,\"lon\":-0.15190381316557336,\"z\":0,\"z_units\":1,\"radius\":20,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":2},{\"abbrev\":\"TemporalAction\",\"action_id\":\"t04\",\"system_id\":27,\"status\":2,\"start_time\":1.49919197955E9,\"duration\":1170.23,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"t04\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"FollowPath\",\"timeout\":10000,\"lat\":0.71878803967636,\"lon\":-0.15192302776731537,\"z\":3,\"z_units\":2,\"speed\":1,\"speed_units\":0,\"points\":[{\"abbrev\":\"PathPoint\",\"x\":-9.116418,\"y\":92.3564,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-63.885708,\"y\":-144.9461,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-34.65332,\"y\":-151.68837,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":17.020351,\"y\":72.20156,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":43.15712,\"y\":52.046722,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-5.4209323,\"y\":-158.43062,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":23.811453,\"y\":-165.17287,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":69.293884,\"y\":31.891886,\"z\":2}],\"custom\":\"Pattern=AreaSurvey;polygon=41.18342663221268:-8.703438157834777:41.1841561882344:-8.70418293315947:41.183748492999065:-8.706521038519504:41.18293385338141:-8.706272224096326:41.18342663221268:-8.703438157834777:41.18342663221268:-8.703438157834777:;angle=1.7976891295541317;corner=A;step=30.0;\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":5},{\"abbrev\":\"TemporalAction\",\"action_id\":\"12_move\",\"system_id\":27,\"status\":2,\"start_time\":1.49919314978E9,\"duration\":195.23,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"12_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"13_communicate\",\"system_id\":27,\"status\":2,\"start_time\":1.49919334501E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"13_communicate\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"z\":0,\"z_units\":1,\"radius\":10,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":3},{\"abbrev\":\"TemporalAction\",\"action_id\":\"14_move\",\"system_id\":27,\"status\":2,\"start_time\":1.49919340501E9,\"duration\":155,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"14_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7187898315558492,\"lon\":-0.15195677562209114,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"15_surface\",\"system_id\":27,\"status\":2,\"start_time\":1.49919356001E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"15_surface\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7187898315558492,\"lon\":-0.15195677562209114,\"z\":0,\"z_units\":1,\"radius\":20,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":2},{\"abbrev\":\"TemporalAction\",\"action_id\":\"t07\",\"system_id\":27,\"status\":2,\"start_time\":1.49919362001E9,\"duration\":1197.59,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"t07\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"FollowPath\",\"timeout\":10000,\"lat\":0.718795104503189,\"lon\":-0.15198085714401655,\"z\":3,\"z_units\":2,\"speed\":1,\"speed_units\":0,\"points\":[{\"abbrev\":\"PathPoint\",\"x\":-33.702625,\"y\":115.752335,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-55.045094,\"y\":50.0608,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-70.04968,\"y\":-93.21271,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-4.359136,\"y\":108.981,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":24.984352,\"y\":102.20967,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-26.530102,\"y\":-56.350426,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":11.853795,\"y\":-35.295624,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":54.32784,\"y\":95.43833,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":83.671326,\"y\":88.667,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":45.79512,\"y\":-27.91493,\"z\":2}],\"custom\":\"Pattern=AreaSurvey;polygon=41.18357415932586:-8.706467918816196:41.18472351019979:-8.706819034780578:41.18471410387161:-8.706872975867132:41.18439444985875:-8.70817542758394:41.183864727974665:-8.708327867688423:41.18310035594353:-8.70918477174141:41.18310035594353:-8.70918477174141:;angle=1.8849555921538435;corner=A;step=30.0;\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":5},{\"abbrev\":\"TemporalAction\",\"action_id\":\"17_move\",\"system_id\":27,\"status\":2,\"start_time\":1.4991948176E9,\"duration\":182.82,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"17_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"18_communicate\",\"system_id\":27,\"status\":2,\"start_time\":1.49919500042E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"18_communicate\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"z\":0,\"z_units\":1,\"radius\":10,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":3},{\"abbrev\":\"TemporalAction\",\"action_id\":\"19_move\",\"system_id\":26,\"status\":2,\"start_time\":1.499188942607E9,\"duration\":223.45,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"19_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7187788082107152,\"lon\":-0.15195341830394685,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"20_surface\",\"system_id\":26,\"status\":2,\"start_time\":1.499189166057E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"20_surface\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7187788082107152,\"lon\":-0.15195341830394685,\"z\":0,\"z_units\":1,\"radius\":20,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":2},{\"abbrev\":\"TemporalAction\",\"action_id\":\"t06\",\"system_id\":26,\"status\":2,\"start_time\":1.499189226058E9,\"duration\":1188.38,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"t06\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"FollowPath\",\"timeout\":10000,\"lat\":0.7187764968163906,\"lon\":-0.1519800372216063,\"z\":3,\"z_units\":2,\"speed\":1,\"speed_units\":0,\"points\":[{\"abbrev\":\"PathPoint\",\"x\":14.557918,\"y\":127.951294,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-58.254684,\"y\":-187.47466,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-19.982666,\"y\":-155.057,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":43.78957,\"y\":121.20586,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":73.02122,\"y\":114.46042,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":18.28935,\"y\":-122.639366,\"z\":2}],\"custom\":\"Pattern=AreaSurvey;polygon=41.18293368072681:-8.70627226408191:41.18357415932586:-8.706467918816196:41.18310035594353:-8.70918477174141:41.18226813890012:-8.71011770295509:41.182264973325786:-8.710116735811676:41.18293368072681:-8.70627226408191:41.18293368072681:-8.70627226408191:;angle=1.7976891295541317;corner=A;step=30.0;\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":5},{\"abbrev\":\"TemporalAction\",\"action_id\":\"22_move\",\"system_id\":26,\"status\":2,\"start_time\":1.499190414438E9,\"duration\":338.88,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"22_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.718813904812872,\"lon\":-0.15195185541838221,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"23_communicate\",\"system_id\":26,\"status\":2,\"start_time\":1.499190753318E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"23_communicate\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.718813904812872,\"lon\":-0.15195185541838221,\"z\":0,\"z_units\":1,\"radius\":10,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":3},{\"abbrev\":\"TemporalAction\",\"action_id\":\"24_move\",\"system_id\":26,\"status\":2,\"start_time\":1.499190813318E9,\"duration\":191.53,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"24_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.7187994229852015,\"lon\":-0.1519169279616982,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"25_surface\",\"system_id\":26,\"status\":2,\"start_time\":1.499191004849E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"25_surface\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7187994229852015,\"lon\":-0.1519169279616982,\"z\":0,\"z_units\":1,\"radius\":20,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":2},{\"abbrev\":\"TemporalAction\",\"action_id\":\"t05\",\"system_id\":26,\"status\":2,\"start_time\":1.49919106485E9,\"duration\":1135.85,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"t05\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"FollowPath\",\"timeout\":10000,\"lat\":0.7188040838396664,\"lon\":-0.15194377049587254,\"z\":3,\"z_units\":2,\"speed\":1,\"speed_units\":0,\"points\":[{\"abbrev\":\"PathPoint\",\"x\":-29.807564,\"y\":129.02394,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-74.87962,\"y\":-66.25058,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-45.64739,\"y\":-72.993484,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-3.671142,\"y\":108.86845,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":22.465279,\"y\":88.71295,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":-16.415155,\"y\":-79.73639,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":12.817078,\"y\":-86.479294,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":48.6017,\"y\":68.55745,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":74.73812,\"y\":48.401955,\"z\":2},{\"abbrev\":\"PathPoint\",\"x\":68.89935,\"y\":23.105518,\"z\":2}],\"custom\":\"Pattern=AreaSurvey;polygon=41.1841561882344:-8.70418293315947:41.18512892186521:-8.705175992544971:41.18512464869761:-8.705200499535247:41.18472726351853:-8.706819991671479:41.183748492999065:-8.706521038519504:41.183748492999065:-8.706521038519504:;angle=1.7976891295541317;corner=A;step=30.0;\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":5},{\"abbrev\":\"TemporalAction\",\"action_id\":\"27_move\",\"system_id\":26,\"status\":2,\"start_time\":1.4991922007E9,\"duration\":62.33,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"27_move\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"Goto\",\"timeout\":10000,\"lat\":0.718813904812872,\"lon\":-0.15195185541838221,\"z\":3,\"z_units\":1,\"speed\":1,\"speed_units\":0,\"roll\":0,\"pitch\":0,\"yaw\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":1},{\"abbrev\":\"TemporalAction\",\"action_id\":\"28_communicate\",\"system_id\":26,\"status\":2,\"start_time\":1.49919226303E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"28_communicate\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.718813904812872,\"lon\":-0.15195185541838221,\"z\":0,\"z_units\":1,\"radius\":10,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":3},{\"abbrev\":\"TemporalAction\",\"action_id\":\"29_communicate\",\"system_id\":27,\"status\":2,\"start_time\":1.49919506042E9,\"duration\":60,\"action\":{\"abbrev\":\"PlanSpecification\",\"plan_id\":\"29_communicate\",\"description\":\"\",\"vnamespace\":\"\",\"variables\":[],\"start_man_id\":\"1\",\"maneuvers\":[{\"abbrev\":\"PlanManeuver\",\"maneuver_id\":\"1\",\"data\":{\"abbrev\":\"StationKeeping\",\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"z\":0,\"z_units\":1,\"radius\":10,\"duration\":60,\"speed\":1,\"speed_units\":0,\"custom\":\"\"},\"start_actions\":[],\"end_actions\":[]}],\"transitions\":[],\"start_actions\":[],\"end_actions\":[]},\"type\":3}],\"depots\":[{\"abbrev\":\"VehicleDepot\",\"vehicle\":28,\"lat\":0.7188139047521422,\"lon\":-0.1519518553327721,\"deadline\":0},{\"abbrev\":\"VehicleDepot\",\"vehicle\":27,\"lat\":0.7188139045699076,\"lon\":-0.15195185503169,\"deadline\":1.499190742607E9},{\"abbrev\":\"VehicleDepot\",\"vehicle\":26,\"lat\":0.718813904812872,\"lon\":-0.15195185541838221,\"deadline\":1.499190742607E9}]}";
		TemporalPlan plan = (TemporalPlan) FormatConversion.fromJson(json);
		PddlPlan pplan = PddlPlan.parse(plan);
		System.out.println(PddlParser.initialState(pplan, 26));
	}

}
