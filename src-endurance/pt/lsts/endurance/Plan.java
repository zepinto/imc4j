package pt.lsts.endurance;

import java.util.ArrayList;
import java.util.Date;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import pt.lsts.imc4j.msg.Goto;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.ScheduledGoto;
import pt.lsts.imc4j.msg.SoiPlan;
import pt.lsts.imc4j.msg.SoiWaypoint;
import pt.lsts.imc4j.util.FormatConversion;
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

	public static Plan parse(String spec) throws Exception {
		Message msg = null;
		try {
			msg = FormatConversion.fromJson(spec);
		}
		catch (Exception e) {
		}
		
		if (msg == null) {
			try {
				JsonObject json = Json.parse(spec).asObject();
				Plan p = new Plan(json.getString("id", ""));
				JsonArray arr = json.get("waypoints").asArray();
				
				for (int i = 0; i < arr.size(); i++) {
					JsonObject wpt = arr.get(i).asObject();
					float lat = wpt.getFloat("latitude", 0);
					float lon = wpt.getFloat("longitude", 0);
					Waypoint waypoint = new Waypoint(i, lat, lon);
					waypoint.setDuration(wpt.getInt("duration", 0));
					double time = wpt.getDouble("eta", 0);
					if (time != 0)
						waypoint.setArrivalTime(new Date((long) (time * 1000)));
					p.addWaypoint(waypoint);
				}
				return p;
			}
			catch (Exception e) {
				throw new Exception("Unrecognized plan format.", e);
			}
			
		}
		if (msg instanceof SoiPlan) {
			return parse((SoiPlan) msg);
		}
		else if (msg instanceof PlanSpecification) {
			return parse((PlanSpecification) msg);
		}
		else {
			throw new Exception("Message not recognized: "+msg.abbrev());
		}		
	}
	

	public static Plan parse(PlanSpecification spec) {
		Plan plan = new Plan(spec.plan_id);
		int id = 1;
		for (Maneuver m : PlanUtilities.getFirstManeuverSequence(spec)) {
			try {
				plan.addWaypoint(new Waypoint(id++, m));
			} catch (Exception e) {
				e.printStackTrace();
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
		if (waypoints != null) {

			for (Waypoint wpt : waypoints) {
				SoiWaypoint waypoint = new SoiWaypoint();
				if (wpt.getArrivalTime() != null)
					waypoint.eta = wpt.getArrivalTime().getTime() / 1000;
				else
					waypoint.eta = 0;
				waypoint.lat = wpt.getLatitude();
				waypoint.lon = wpt.getLongitude();
				waypoint.duration = wpt.getDuration();
				plan.waypoints.add(waypoint);
			}
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
	
	public ArrayList<Waypoint> waypoints() {
		ArrayList<Waypoint> ret = new ArrayList<>();
		synchronized (waypoints) {
			for (Waypoint wpt : waypoints)
				ret.add(wpt.clone());
		}
		return ret;
	}

	public void remove(int index) {
		synchronized (waypoints) {
			waypoints.remove(index);
		}
	}
	
	public void removeSchedule() {
		synchronized (waypoints) {
			for (Waypoint waypoint : waypoints) {
				waypoint.setArrivalTime(null);
			}
		}
	}

	public void scheduleWaypoints(long startTime, double minDuration, double lat, double lon, double speed) {
		long curTime = startTime + (long)(minDuration * 1000);
		synchronized (waypoints) {
			for (Waypoint waypoint : waypoints) {
				// skip waypoints in the past
				if (waypoint.getArrivalTime() != null && waypoint.getArrivalTime().before(new Date()))
					continue;
				
				double distance = WGS84Utilities.distance(lat, lon, waypoint.getLatitude(), waypoint.getLongitude());
				double timeToReach = distance / speed;
				waypoint.setDuration(Math.max(waypoint.getDuration(), (int)minDuration));
				curTime += (long) (1000.0 * timeToReach);
				waypoint.setArrivalTime(new Date(curTime));
				
				curTime += (long) (1000.0 * waypoint.getDuration());
				lat = waypoint.getLatitude();
				lon = waypoint.getLongitude();
			}
		}
	}

	public void scheduleWaypoints(long startTime, double minDuration, double speed) {
		if (waypoints.isEmpty())
			return;

		Waypoint start = waypoints.get(0);
		scheduleWaypoints(startTime, minDuration, start.getLatitude(), start.getLongitude(), speed);
	}

	public String toString() {
		JsonObject pp = new JsonObject();
		pp.add("id", getPlanId());
		JsonArray waypoints = new JsonArray();
		for (Waypoint wpt : waypoints()) {
			JsonObject waypoint = new JsonObject();
			waypoint.add("latitude", wpt.getLatitude());
			waypoint.add("longitude", wpt.getLongitude());
			if (wpt.getDuration() != 0)
				waypoint.add("duration", wpt.getDuration());
			if (wpt.getArrivalTime() != null)
				waypoint.add("eta", wpt.getArrivalTime().getTime() / 1000);										
			waypoints.add(waypoint);
		}
		pp.add("waypoints", waypoints);				
		return pp.toString();		
	}

	public void remove(Waypoint waypoint) {
		remove(waypoint.getId());
	}

	public boolean scheduledInTheFuture() {
		long present = System.currentTimeMillis();
		if (waypoints == null)
			return false;

		synchronized (waypoints) {
			for (Waypoint wpt : waypoints) {
				if (wpt.getArrivalTime() == null || wpt.getArrivalTime().getTime() < present)
					return false;
			}
		}
		return true;
	}
	
	public Date getETA() {
		if (!scheduledInTheFuture())
			return null;
		
		synchronized (waypoints) {
			Waypoint wpt = 	waypoints.get(waypoints.size()-1);
			if (wpt != null)
				return wpt.getArrivalTime();
		}
	
		return null;
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

		System.out.println(plan.toString());
		
		Plan plan2 = Plan.parse(plan.toString());
		System.out.println(plan2.toString());
	}
}
