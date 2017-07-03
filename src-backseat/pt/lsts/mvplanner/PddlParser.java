package pt.lsts.mvplanner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.PlanManeuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.SetEntityParameters;

public class PddlParser {

	public static String initialState(PddlPlan plan, int vehicle) {

		ArrayList<PddlLocation> locations = plan.locations(vehicle);

		// start printing...
		StringBuilder sb = new StringBuilder();
		sb.append("(define (problem LSTSprob)(:domain LSTS)\n(:objects\n  ");

		// print location names
		for (PddlLocation loc : locations) {
			sb.append(" " + loc.name);
		}
		sb.append(" - location\n  ");

		// print vehicle names
		sb.append(VehicleParams.vehicleNickname(vehicle) + " - auv\n  ");

		// print payload names
		for (PayloadRequirement pr : VehicleParams.payloadsFor(vehicle))
			sb.append(VehicleParams.vehicleNickname(vehicle) + "_" + pr.name() + " - " + pr.name());

		ArrayList<IPddlAction> allActions = new ArrayList<>();

		// Names of survey tasks' areas
		for (IPddlAction action : plan.surveyActions(vehicle)) {
			allActions.add(action);
			sb.append(action.getAssociatedTask() + "_area ");
		}
		sb.append(" - area\n  ");

		// Names of sample tasks' objects of interest
		for (IPddlAction action : plan.sampleActions(vehicle)) {
			allActions.add(action);
			sb.append(action.getAssociatedTask() + "_obj ");
		}
		sb.append(" - oi\n");

		// Task names
		for (IPddlAction action : allActions) {
			sb.append(action.getAssociatedTask() + " ");
		}
		sb.append(" - task\n");

		sb.append(")\n(:init\n");

		// distance between all locations
		sb.append(distances(locations));

		// details of vehicle
		sb.append(vehicleDetails(vehicle));

		// survey tasks
		sb.append(surveyTasks(vehicle, plan.surveyActions(vehicle)));

		// sample tasks
		sb.append(sampleTasks(vehicle, plan.sampleActions(vehicle)));
		sb.append(")\n");

		// goals to solve
		sb.append(goals(plan, vehicle));

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

	protected static String vehicleDetails(int v) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n  ;" + VehicleParams.vehicleNickname(v) + ":\n");
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

	private static String surveyTasks(int vehicle, ArrayList<SurveyAction> actions) {

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
	
	private static String sampleTasks(int vehicle, ArrayList<SampleAction> actions) {
		StringBuilder sb = new StringBuilder();
		
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
	
	private static String goals(PddlPlan plan, int vehicle) {
        StringBuilder sb = new StringBuilder();
        sb.append("(:goal (and\n");
        for (SampleAction t : plan.sampleActions(vehicle)) {
            for (PayloadRequirement r : payloads(t)) {
                sb.append("  (communicated_data " + t.getAssociatedTask() + "_" + r.name() + ")\n");
            }
        }
        
        for (SurveyAction t : plan.surveyActions(vehicle)) {
            for (PayloadRequirement r : payloads(t)) {
                sb.append("  (communicated_data " + t.getAssociatedTask() + "_" + r.name() + ")\n");
            }
        }
        
        sb.append("))\n");
        sb.append("(:metric minimize (total-time)))\n");
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
            
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Unrecognized PDDL syntax on line '" + line + "'", e);
        }

        
        for (IPddlAction act : actions) {
        	if (act.getAssociatedTask().equals(name))
        		action = (AbstractPddlAction) act;
        }
        
        if (action == null) {
        	System.err.println("Could not find action named "+name);
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

}
