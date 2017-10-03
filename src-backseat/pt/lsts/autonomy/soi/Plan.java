package pt.lsts.autonomy.soi;

import java.util.ArrayList;
import java.util.Date;

import pt.lsts.imc4j.msg.Goto;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.ScheduledGoto;
import pt.lsts.imc4j.msg.SoiPlan;
import pt.lsts.imc4j.msg.SoiWaypoint;
import pt.lsts.imc4j.util.PlanUtilities;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.WGS84Utilities;

public class Plan {

	private final String planId;
	private boolean cyclic = false;
	private ArrayList<Waypoint> waypoints = new ArrayList<>();
	
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

	public Plan(String id) {
		this.planId = id;
	}

	public static Plan parse(PlanSpecification spec) {
		Plan plan = new Plan(spec.plan_id);
		int id = 1;
		for (Maneuver m : PlanUtilities.getFirstManeuverSequence(spec)) {
			if (m instanceof ScheduledGoto)
				plan.addWaypoint(new Waypoint(id++, (ScheduledGoto) m));
			else {
				try {
					plan.addWaypoint(new Waypoint(id++, m));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (PlanUtilities.isCyclic(spec))
			plan.cyclic = true;
		
		return plan;
	}
	
	public static Plan parse(SoiPlan spec) {
		Plan plan = new Plan("soi_"+spec.plan_id);
		int id = 1;
		for (SoiWaypoint wpt : spec.waypoints) {
			Waypoint soiWpt = new Waypoint(id++, wpt.lat, wpt.lon);
			soiWpt.setDuration(wpt.duration);
			if (wpt.eta > 0)
				soiWpt.setArrivalTime(new Date(1000 * wpt.eta));
			plan.addWaypoint(soiWpt);
		}
		return plan;		
	}
	
	public SoiPlan asImc() {
		SoiPlan plan = new SoiPlan();
		for (Waypoint wpt : waypoints) {
			SoiWaypoint waypoint = new SoiWaypoint();
			waypoint.eta = wpt.getArrivalTime().getTime() / 1000;
			waypoint.lat = wpt.getLatitude();
			waypoint.lon = wpt.getLongitude();
			waypoint.duration = wpt.getDuration();
			plan.waypoints.add(waypoint);
		}
		byte[] data = plan.serializeFields();
		plan.plan_id = SerializationUtils.crc16(data, 2, data.length-2);
		return plan;
	}
	
	public int checksum() {
		byte[] data = asImc().serializeFields();
		return SerializationUtils.crc16(data, 2, data.length-2);
	}

	public void addWaypoint(Waypoint waypoint) {
		synchronized (waypoints) {
			waypoints.add(waypoint);			
		}
	}
	
	public Waypoint waypoint(int index) {
		if (index < 0 || index >= waypoints.size())
			return null;
		return waypoints.get(index);
	}

	public void remove(int index) {
		synchronized (waypoints) {
			waypoints.remove(index);
		}
	}

	public void scheduleWaypoints(long startTime, double lat, double lon, double speed) {
		long curTime = startTime;
		synchronized (waypoints) {
			for (Waypoint waypoint : waypoints) {
				double distance = WGS84Utilities.distance(lat, lon, waypoint.getLatitude(), waypoint.getLongitude());
				double timeToReach = distance / speed;
				curTime += (long) (1000.0 * (timeToReach + waypoint.getDuration()));
				waypoint.setArrivalTime(new Date(curTime));
				lat = waypoint.getLatitude();
				lon = waypoint.getLongitude();
			}
		}
	}

	public void scheduleWaypoints(long startTime, double speed) {
		if (waypoints.isEmpty())
			return;

		Waypoint start = waypoints.get(0);
		scheduleWaypoints(startTime, start.getLatitude(), start.getLongitude(), speed);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Plan '"+planId+"'"+(cyclic? " (cyclic):\n": ":\n"));
		synchronized (waypoints) {
			for (Waypoint wpt : waypoints) {
				sb.append("\t"+wpt.getId() + ", " + (float) wpt.getLatitude() + ", " + (float) wpt.getLongitude() + ", "
						+ wpt.getArrivalTime() + ", "+wpt.getDuration()+"\n");
			}
		}

		return sb.toString();

	}

	public void remove(Waypoint waypoint) {
		remove(waypoint.getId());
	}
	
	public static void main(String[] args) throws Exception {
		Plan plan = new Plan("test");
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

		plan.addWaypoint(new Waypoint(1, goto1));
		plan.addWaypoint(new Waypoint(2, goto2));
		plan.addWaypoint(new Waypoint(3, goto3));
		plan.addWaypoint(new Waypoint(4, goto4));

		System.out.println(plan);
	}
}
