package pt.lsts.mvplanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalPlan;
import pt.lsts.imc4j.msg.VehicleDepot;
import pt.lsts.imc4j.util.PlanUtilities;

public class PddlPlan {

	protected LinkedHashMap<Integer, PddlLocation> depots = new LinkedHashMap<>();
	protected LinkedHashMap<Integer, Date> deadlines = new LinkedHashMap<>();
	protected ArrayList<IPddlAction> actions = new ArrayList<>();

	public static PddlPlan parse(TemporalPlan plan) {
		PddlPlan result = new PddlPlan();
		for (VehicleDepot depot : plan.depots) {
			PddlLocation loc = new PddlLocation("depot_" + depot.vehicle, Math.toDegrees(depot.lat),
					Math.toDegrees(depot.lon));
			result.depots.put(depot.vehicle, loc);
			result.deadlines.put(depot.vehicle, new Date((long) (depot.deadline * 1000)));
		}
		for (TemporalAction action : plan.actions)
			result.actions.add(createAction(action));

		Collections.sort(result.actions);
		return result;
	}

	public TemporalPlan asPlan() {
		TemporalPlan plan = new TemporalPlan();

		for (Entry<Integer, PddlLocation> depot : depots.entrySet()) {
			VehicleDepot d = new VehicleDepot();
			d.lat = Math.toRadians(depot.getValue().latDegs);
			d.lon = Math.toRadians(depot.getValue().lonDegs);
			d.vehicle = depot.getKey();
			d.deadline = deadlines.get(depot.getKey()).getTime() / 1000.0;
			plan.depots.add(d);
		}

		for (IPddlAction action : actions)
			plan.actions.add(action.asImc());

		return plan;
	}

	public static IPddlAction createAction(TemporalAction action) {
		PddlLocation loc = null;
		Date start = null, end = null;

		try {
			double[] pos = PlanUtilities.computeLocations(action.action).iterator().next();
			loc = new PddlLocation(action.action_id + "_loc", pos[0], pos[1]);
			start = new Date((long) (action.start_time * 1000));
			end = new Date((long) ((action.start_time + action.duration) * 1000));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		AbstractPddlAction a;
		switch (action.type) {
		case ATYPE_COMMUNICATE:
			a = new CommunicateAction(loc, start, end);
			break;
		case ATYPE_SURFACE:
			a = new SurfaceAction(loc, start, end);
			break;
		case ATYPE_LOCATE:
			a = new LocateAction(loc, start, end);
			break;
		case ATYPE_MOVE:
			a = new MoveAction(loc, start, end);
			break;
		case ATYPE_SAMPLE:
			a = new SampleAction(loc, start, end, action.action);
			break;
		case ATYPE_SURVEY:
			a = new SurveyAction(loc, start, end, action.action);
			break;
		default:
			System.err.println("Unrecognized action type: " + action.type);
			return null;
		}

		a.setTask(action.action_id);
		a.setVehicle(action.system_id);

		return a;
	}

	public ArrayList<IPddlAction> actions(int vehicle_id) {
		ArrayList<IPddlAction> ret = new ArrayList<>();
		for (IPddlAction act : actions) {
			if (act.getAssociatedVehicle() == vehicle_id)
				ret.add(act);
		}

		return ret;
	}

}
