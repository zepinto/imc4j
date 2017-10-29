package pt.lsts.endurance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

	

	public int checksum() {
		byte[] data = ImcTranslation.toImc(this).serializeFields();
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

	/**
	 * @return the waypoints
	 */
	public final List<Waypoint> getWaypoints() {
		return Collections.unmodifiableList(waypoints);
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
}
