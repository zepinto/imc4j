package pt.lsts.autonomy.soi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import pt.lsts.imc4j.msg.Goto;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.ScheduledGoto;
import pt.lsts.imc4j.util.PlanUtilities;
import pt.lsts.imc4j.util.WGS84Utilities;

public class SoiPlan {

	private final String planId;
	private boolean cyclic = false;
	private ArrayList<SoiWaypoint> waypoints = new ArrayList<>();
	private double comms_duration = 60;
	
	/**
	 * @return the planId
	 */
	public final String getPlanId() {
		return planId;
	}

	/**
	 * @return the cyclic
	 */
	public final boolean isCyclic() {
		return cyclic;
	}

	/**
	 * @param cyclic the cyclic to set
	 */
	public final void setCyclic(boolean cyclic) {
		this.cyclic = cyclic;
	}

	public SoiPlan(String id) {
		this.planId = id;
	}

	public static SoiPlan parse(PlanSpecification spec) {
		SoiPlan plan = new SoiPlan(spec.plan_id);
		int id = 1;
		for (Maneuver m : PlanUtilities.getFirstManeuverSequence(spec)) {
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

		if (PlanUtilities.isCyclic(spec))
			plan.cyclic = true;
		
		return plan;
	}

	public void addWaypoint(SoiWaypoint waypoint) {
		synchronized (waypoints) {
			waypoints.add(waypoint);
			Collections.sort(waypoints);
		}
	}

	public SoiWaypoint pollWaypoint() {
		synchronized (waypoints) {
			if (waypoints.isEmpty())
				return null;
			SoiWaypoint wpt = waypoints.get(0);
			waypoints.remove(0);
			return wpt;
		}
	}

	public SoiWaypoint currentWaypoint() {
		synchronized (waypoints) {
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

	public void scheduleWaypoints(long startTime, double lat, double lon, double speed) {
		double duration = comms_duration;
		long curTime = startTime + (long)(comms_duration * 1000.0);
		synchronized (waypoints) {
			for (SoiWaypoint waypoint : waypoints) {
				double distance = WGS84Utilities.distance(lat, lon, waypoint.getLatitude(), waypoint.getLongitude());
				double timeToReach = distance / speed;
				curTime += (long) (1000.0 * (timeToReach + duration + comms_duration));
				waypoint.setArrivalTime(new Date(curTime));
				lat = waypoint.getLatitude();
				lon = waypoint.getLongitude();
			}
		}
	}

	public void scheduleWaypoints(long startTime, double speed) {
		if (waypoints.isEmpty())
			return;

		SoiWaypoint start = waypoints.get(0);
		scheduleWaypoints(startTime, start.getLatitude(), start.getLongitude(), speed);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Plan '"+planId+"'"+(cyclic? " (cyclic):\n": ":\n"));
		synchronized (waypoints) {
			for (SoiWaypoint wpt : waypoints) {
				sb.append("\t"+wpt.getId() + ", " + (float) wpt.getLatitude() + ", " + (float) wpt.getLongitude() + ", "
						+ wpt.getArrivalTime() + ", "+wpt.getDuration()+"\n");
			}
		}

		return sb.toString();

	}

	public void remove(SoiWaypoint waypoint) {
		remove(waypoint.getId());
	}

	public static void main(String[] args) throws Exception {
		SoiPlan plan = new SoiPlan("test");
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
