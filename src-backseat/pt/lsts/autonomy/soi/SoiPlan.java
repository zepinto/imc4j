package pt.lsts.autonomy.soi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import pt.lsts.imc4j.msg.Goto;
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
				plan.addWaypoint(new SoiWaypoint(id++, (ScheduledGoto) m));
			else {
				try {
					plan.addWaypoint(new SoiWaypoint(id++, m));
				} catch (Exception e) {
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

	public String toString() {
		StringBuilder sb = new StringBuilder();
		synchronized (waypoints) {
			Collections.sort(waypoints);
			for (SoiWaypoint wpt : waypoints) {
				String arrival = wpt.getArrivalTime() != null ? "" + (wpt.getArrivalTime().getTime() / 1000) : "_";
				sb.append(wpt.getId() + ", " + (float) wpt.getLatitude() + ", " + (float) wpt.getLongitude() + ", "
						+ arrival + ", " + (float) wpt.getPeriodicity() + "\n");
			}
		}

		return sb.toString();

	}

	public void remove(SoiWaypoint waypoint) {
		remove(waypoint.getId());
	}

	public static void main(String[] args) throws Exception {
		SoiPlan plan = new SoiPlan();
		ScheduledGoto goto1 = new ScheduledGoto();
		goto1.lat = Math.toRadians(41);
		goto1.lon = Math.toRadians(-8);
		goto1.arrival_time = new Date().getTime() / 1000.0 + 3600;

		ScheduledGoto goto2 = new ScheduledGoto();
		goto2.lat = Math.toRadians(41.5);
		goto2.lon = Math.toRadians(-8.5);
		goto2.arrival_time = new Date().getTime() / 1000.0 + 1800;

		Goto goto3 = new Goto();
		goto3.lat = Math.toRadians(41.2);
		goto3.lon = Math.toRadians(-8.2);

		Goto goto4 = new Goto();
		goto4.lat = Math.toRadians(41.4);
		goto4.lon = Math.toRadians(-8.4);

		plan.addWaypoint(new SoiWaypoint(1, goto1));
		plan.addWaypoint(new SoiWaypoint(2, goto2));
		plan.addWaypoint(new SoiWaypoint(3, goto3));
		plan.addWaypoint(new SoiWaypoint(4, goto4));

		System.out.println(plan);
	}
}
