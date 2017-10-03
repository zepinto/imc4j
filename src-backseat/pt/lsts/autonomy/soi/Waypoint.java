package pt.lsts.autonomy.soi;

import java.util.Date;

import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.ScheduledGoto;

public class Waypoint implements Comparable<Waypoint> {

	private int id, duration = 0;
	private float latitude, longitude;
	private Date arrivalTime = null;
	
	public Waypoint(int id, ScheduledGoto man) {
		this.id = id;
		this.latitude = (float) Math.toDegrees(man.lat);
		this.longitude = (float) Math.toDegrees(man.lon);
		this.arrivalTime = new Date((long) (man.arrival_time * 1000));
	}

	public Waypoint(int id, Maneuver man) throws Exception {
		this.id = id;
		this.latitude = (float) Math.toDegrees(man.getDouble("lat"));
		this.longitude = (float )Math.toDegrees(man.getDouble("lon"));
	}

	public Waypoint(int id, float lat, float lon) {
		this.latitude = lat;
		this.longitude = lon;
		this.id = id;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getId() {
		return id;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	@Override
	public int compareTo(Waypoint o) {
		
		if (arrivalTime == null && o.arrivalTime == null)
			return new Long(getId()).compareTo(new Long(o.getId()));
		
		if (arrivalTime == null && o.arrivalTime != null)
			return 1;

		if (arrivalTime != null && o.arrivalTime == null)
			return -1;

		return arrivalTime.compareTo(o.arrivalTime);
	}

	private Date nextSchedule() {
		return arrivalTime;
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		Waypoint wpt = new Waypoint(0, 41, -8);
		wpt.arrivalTime = new Date(17, 8, 24, 17, 42, 00);
		System.out.println(wpt.nextSchedule());
	}

}
