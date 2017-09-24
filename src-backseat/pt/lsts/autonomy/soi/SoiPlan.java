package pt.lsts.autonomy.soi;

import java.util.ArrayList;
import java.util.Collections;

import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.ScheduledGoto;
import pt.lsts.imc4j.util.PlanUtilities;

public class SoiPlan {

	private ArrayList<SoiWaypoint> waypoints = new ArrayList<>();

	public static SoiPlan parse(PlanSpecification spec) {
		SoiPlan plan = new SoiPlan();
		int id = 1;
		for (Maneuver m : PlanUtilities.getManeuverSequence(spec)) {
			if (m instanceof ScheduledGoto)
				plan.addWaypoint(new SoiWaypoint(id++, (ScheduledGoto)m));
			else {
				try {
					plan.addWaypoint(new SoiWaypoint(id++, m));
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return plan;
	}
	
	public void addWaypoint(SoiWaypoint waypoint) {
		synchronized (waypoints) {
			waypoints.add(waypoint);
		}
	}

	public SoiWaypoint pollWaypoint() {
		synchronized (waypoints) {
			Collections.sort(waypoints);
			if (waypoints.isEmpty())
				return null;
			SoiWaypoint wpt = waypoints.get(0);
			waypoints.remove(0);
			return wpt;
		}
	}
	
	public SoiWaypoint currentWaypoint() {
		synchronized (waypoints) {
			Collections.sort(waypoints);
			return waypoints.get(0);
		}
	}

	public void remove(int waypoint) {
		synchronized (waypoints) {
			for (int i = 0; i < waypoints.size(); i++)
				if (waypoints.get(i).getId() == waypoint) {
					waypoints.remove(i);
					return;
				}
		}
	}

	public void remove(SoiWaypoint waypoint) {
		remove(waypoint.getId());
	}
}
