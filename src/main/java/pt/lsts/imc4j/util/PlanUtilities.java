package pt.lsts.imc4j.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import pt.lsts.imc4j.msg.CommsRelay;
import pt.lsts.imc4j.msg.CompassCalibration;
import pt.lsts.imc4j.msg.Elevator;
import pt.lsts.imc4j.msg.FollowPath;
import pt.lsts.imc4j.msg.FollowTrajectory;
import pt.lsts.imc4j.msg.Goto;
import pt.lsts.imc4j.msg.Launch;
import pt.lsts.imc4j.msg.Loiter;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PathPoint;
import pt.lsts.imc4j.msg.PlanManeuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.PlanTransition;
import pt.lsts.imc4j.msg.PopUp;
import pt.lsts.imc4j.msg.Rows;
import pt.lsts.imc4j.msg.ScheduledGoto;
import pt.lsts.imc4j.msg.StationKeeping;
import pt.lsts.imc4j.msg.TrajectoryPoint;
import pt.lsts.imc4j.msg.YoYo;
import pt.lsts.imc4j.util.PlanUtilities.Waypoint.TYPE;

/**
 * This class provides some utility methods to work with IMC plans
 * 
 * @author zp
 */
public class PlanUtilities {


	/**
	 * This method calculates the maneuver sequence present in a plan. In case
	 * of cyclic plans, it will retrieve the first sequence of maneuvers that
	 * include one repeated maneuver.
	 * 
	 * @param plan
	 *            The plan to parsed.         
	 * @return a maneuver sequence.
	 * @see #getFirstManeuverSequence(PlanSpecification)
	 */
	public static List<Maneuver> getManeuverCycleOrSequence(PlanSpecification plan) {
		ArrayList<Maneuver> ret = new ArrayList<Maneuver>();

		LinkedHashMap<String, Maneuver> maneuvers = new LinkedHashMap<String, Maneuver>();
		LinkedHashMap<String, String> transitions = new LinkedHashMap<String, String>();

		for (PlanManeuver m : plan.maneuvers)
			maneuvers.put(m.maneuver_id, m.data);

		for (PlanTransition pt : plan.transitions) {
			if (transitions.containsKey(pt.source_man))				
				continue;
			transitions.put(pt.source_man, pt.dest_man);
		}

		Vector<String> visited = new Vector<String>();
		String man = plan.start_man_id;

		while (man != null) {
			if (visited.contains(man)) {
				ret.add(maneuvers.get(man));
				return ret;
			}
			visited.add(man);
			Maneuver m = maneuvers.get(man);
			ret.add(m);
			man = transitions.get(man);
		}

		return ret;
		
	}
	/**
	 * This method calculates the maneuver sequence present in a plan. In case
	 * of cyclic plans, it will retrieve the first non-repeating sequence of
	 * maneuvers.
	 * 
	 * @param plan
	 *            The plan to parsed.
	 * @return a maneuver sequence.
	 */
	public static List<Maneuver> getFirstManeuverSequence(PlanSpecification plan) {
		ArrayList<Maneuver> ret = new ArrayList<Maneuver>();

		LinkedHashMap<String, Maneuver> maneuvers = new LinkedHashMap<String, Maneuver>();
		LinkedHashMap<String, String> transitions = new LinkedHashMap<String, String>();

		for (PlanManeuver m : plan.maneuvers)
			maneuvers.put(m.maneuver_id, m.data);

		for (PlanTransition pt : plan.transitions) {
			if (transitions.containsKey(pt.source_man)) {
				System.err
						.println("This should be used only in sequential plans");
				continue;
			}
			transitions.put(pt.source_man, pt.dest_man);
		}

		Vector<String> visited = new Vector<String>();
		String man = plan.start_man_id;

		while (man != null) {
			if (visited.contains(man)) {
				return ret;
			}
			visited.add(man);
			Maneuver m = maneuvers.get(man);
			ret.add(m);
			man = transitions.get(man);
		}

		return ret;
	}

	/**
	 * Given a PlanSpecification message, computes its list of WGS84 locations
	 * 
	 * @param plan
	 *            a PlanSpecification message
	 * @return a Collection of locations of the type double[2] = {latitude,
	 *         longitude}. <br/>
	 *         Latitude and Longitude are represented in decimal degrees.
	 */
	public static Collection<double[]> computeLocations(PlanSpecification plan) {
		ArrayList<double[]> locations = new ArrayList<double[]>();

		for (Maneuver m : getManeuverCycleOrSequence(plan))
			locations.addAll(computeLocations(m));

		return locations;
	}

	/**
	 * This method parses an IMC plan and calculates its waypoints.
	 * 
	 * @param plan
	 *            An IMC plan to be parsed
	 * @return A list of waypoints found in the plan.
	 * @see PlanUtilities.Waypoint
	 */
	public static List<Waypoint> computeWaypoints(PlanSpecification plan) {
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();

		for (Maneuver m : getManeuverCycleOrSequence(plan))
			waypoints.addAll(computeWaypoints(m));

		return waypoints;
	}

	/**
	 * Similar to {@link #computeLocations(Maneuver)} but in this case returning
	 * waypoint structures
	 * 
	 * @param m
	 *            The Maneuver to be converted to a list of waypoints
	 * @return a Collection of waypoints
	 * @see PlanUtilities.Waypoint
	 */
	public static Collection<Waypoint> computeWaypoints(Maneuver m) {
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		Collection<double[]> path = null;
		Waypoint start = getStartLocation(m);

		if (start == null)
			return waypoints;

		start.setSpeed(getSpeed(m));
		
		switch (m.mgid()) {
		case Goto.ID_STATIC:
		case YoYo.ID_STATIC:
		case PopUp.ID_STATIC:
		case ScheduledGoto.ID_STATIC:
		case Launch.ID_STATIC:
			start.setType(TYPE.REGULAR);			
			waypoints.add(start);
			return waypoints;
		case Loiter.ID_STATIC:
			if (((Loiter) m).direction == Loiter.DIRECTION.LD_CLOCKW)
				start.setType(TYPE.LOITER_CW);
			else
				start.setType(TYPE.LOITER_CCW);
			waypoints.add(start);
			return waypoints;
		case CompassCalibration.ID_STATIC:
			if (((CompassCalibration) m).direction == CompassCalibration.DIRECTION.LD_CLOCKW)
				start.setType(TYPE.LOITER_CW);
			else
				start.setType(TYPE.LOITER_CCW);
			waypoints.add(start);
			return waypoints;
		case StationKeeping.ID_STATIC:
			start.setType(TYPE.STATION_KEEP);
			waypoints.add(start);
			return waypoints;
		case CommsRelay.ID_STATIC:
			start.setType(TYPE.OTHER);
			waypoints.add(start);
			return waypoints;
		case Elevator.ID_STATIC:
			start.setType(TYPE.LOITER_CW);
			waypoints.add(start);
			Waypoint end = start.copy();
			end.setSpeed(getSpeed(m));
			end.setDepth(Float.NaN);
			end.setAltitude(Float.NaN);
			end.setHeight(Float.NaN);
			Elevator elev = (Elevator) m;
			switch (elev.end_z_units) {
			case ALTITUDE:
				end.setAltitude((float) elev.end_z);
				break;
			case DEPTH:
				end.setDepth((float) elev.end_z);
				break;
			case HEIGHT:
				end.setHeight((float) elev.end_z);
				break;
			default:
				break;
			}
			waypoints.add(end);
			return waypoints;
		case FollowPath.ID_STATIC:
			path = computePath((FollowPath) m);
			break;
		case FollowTrajectory.ID_STATIC:
			path = computePath((FollowTrajectory) m);
			break;
		case Rows.ID_STATIC:
			path = computePath((Rows) m);
			break;
		default:
			// return empty set of waypoints for other maneuvers
			return waypoints;
		}

		start.setType(TYPE.REGULAR);
		for (double[] p : path) {
			Waypoint wpt = start.copy();
			wpt.setLatitude(p[0]);
			wpt.setLongitude(p[1]);
			wpt.setSpeed(getSpeed(m));
			if (!Float.isNaN(wpt.getDepth()))
				wpt.setDepth((float) (wpt.getDepth() + p[2]));
			if (!Float.isNaN(wpt.getAltitude()))
				wpt.setAltitude((float) (wpt.getAltitude() + p[2]));
			if (!Float.isNaN(wpt.getHeight()))
				wpt.setHeight((float) (wpt.getHeight() + p[2]));
			waypoints.add(wpt);
		}

		return waypoints;
	}

	public static float getSpeed(Maneuver m) {
		if (m.getTypeOf("speed") == null)
			return 0;
		
		if (m.getTypeOf("speed_units") == null)
			return 0;
		if (m.getString("speed_units").equals("RPM"))
			return m.getFloat("speed") / 900.0f;		
		else if (m.getString("speed_units").equals("METERS_PS"))
			return m.getFloat("speed");
		return 0;
	}
	
	/**
	 * Compute the start location for a given maneuver
	 * 
	 * @param m
	 *            The maneuver
	 * @return The first waypoint for the maneuver or <code>null</code> if no
	 *         waypoint can be calculated.
	 */
	public static Waypoint getStartLocation(Maneuver m) {
		Waypoint wpt = new Waypoint();
		if (m.getTypeOf("lat") == null)
			return null;
		wpt.setLatitude(Math.toDegrees(m.getDouble("lat")));
		wpt.setLongitude(Math.toDegrees(m.getDouble("lon")));
		wpt.setRadius(m.getFloat("radius"));
		wpt.setTime(m.getFloat("duration"));
		wpt.setDepth(Float.NaN);
		wpt.setAltitude(Float.NaN);
		wpt.setHeight(Float.NaN);

		String zfield = "z", zunitsField = "z_units";
		if (m.getTypeOf("start_z") != null) {
			zfield = "start_z";
			zunitsField = "start_z_units";
		}

		if (m.getTypeOf(zfield) != null && m.getTypeOf(zunitsField) != null) {
			if ("ALTITUDE".equals(m.getString(zunitsField)))
				wpt.setAltitude(m.getFloat(zfield));
			else if ("DEPTH".equals(m.getString(zunitsField)))
				wpt.setDepth(m.getFloat(zfield));
			else if ("HEIGHT".equals(m.getString(zunitsField)))
				wpt.setHeight(m.getFloat(zfield));
		}
		return wpt;
	}

	private static Collection<double[]> computeSingleLoc(Maneuver m) {
		return Arrays.asList(new double[] { Math.toDegrees(m.getDouble("lat")),
				Math.toDegrees(m.getDouble("lon")) });
	}

	private static Collection<double[]> computePath(FollowPath man) {
		double refLat = Math.toDegrees(man.lat), refLon = Math
				.toDegrees(man.lon);
		Collection<PathPoint> path = man.points;

		Vector<double[]> ret = new Vector<double[]>();
		for (PathPoint p : path)
			ret.add(WGS84Utilities.WGS84displace(refLat, refLon, 0, p.x,
					p.y, p.z));

		return ret;
	}

	private static Collection<double[]> computePath(FollowTrajectory man) {
		double refLat = Math.toDegrees(man.lat), refLon = Math
				.toDegrees(man.lon);
		Collection<TrajectoryPoint> path = man.points;

		Vector<double[]> ret = new Vector<double[]>();
		for (TrajectoryPoint p : path)
			ret.add(WGS84Utilities.WGS84displace(refLat, refLon, 0, p.x,
					p.y, p.z));

		return ret;
	}

	/**
	 * XY Coordinate conversion considering a rotation angle. (Eduardo Marques)
	 * 
	 * @param angleRadians
	 *            angle
	 * @param x
	 *            original x value on entry, rotated x value on exit.
	 * @param y
	 *            original y value on entry, rotated y value on exit.
	 * @param clockwiseRotation
	 *            clockwiseRotation rotation or not
	 */
	private static double[] rotate(double angleRadians, double x, double y,
			boolean clockwiseRotation) {
		double sina = Math.sin(angleRadians), cosa = Math.cos(angleRadians);
		double[] xy = { 0, 0 };
		if (clockwiseRotation) {
			xy[0] = x * cosa + y * sina;
			xy[1] = -x * sina + y * cosa;
		} else {
			xy[0] = x * cosa - y * sina;
			xy[1] = x * sina + y * cosa;
		}
		return xy;
	}

	private static Vector<double[]> calcRowsPoints(double width, double length,
			double hstep, double alternationPercent, double curvOff,
			boolean squareCurve, double bearingRad, double crossAngleRadians,
			boolean invertY) {
		width = Math.abs(width);
		length = Math.abs(length);
		hstep = Math.abs(hstep);

		boolean direction = true;
		Vector<double[]> newPoints = new Vector<double[]>();
		double[] point = { -curvOff, 0, 0, -1 };
		newPoints.add(point);

		double x2;
		for (double y = 0; y <= width; y += hstep) {
			if (direction) {
				x2 = length + curvOff;
			} else {
				x2 = -curvOff;
			}
			direction = !direction;

			double hstepDelta = 0;
			if (direction)
				hstepDelta = hstep * (1 - alternationPercent);
			point = new double[] { x2, y - hstepDelta, 0, -1 };
			newPoints.add(point);

			if (y + hstep <= width) {
				double hstepAlt = hstep;
				if (!direction)
					hstepAlt = hstep * alternationPercent;
				point = new double[] {
						x2 + (squareCurve ? 0 : 1)
								* (direction ? curvOff : -curvOff),
						y + hstepAlt, 0, -1 };
				newPoints.add(point);
			}
		}

		for (double[] pt : newPoints) {
			double[] res = rotate(-crossAngleRadians, pt[0], 0, false);
			pt[0] = res[0];
			pt[1] = pt[1] + res[1];
			if (invertY)
				pt[1] = -pt[1];
			res = rotate(bearingRad + (!invertY ? -1 : 1) * -crossAngleRadians,
					pt[0], pt[1], false);
			pt[0] = res[0];
			pt[1] = res[1];
		}

		return newPoints;
	}

	private static Collection<double[]> computePath(Rows man) {
		double refLat = Math.toDegrees(man.lat), refLon = Math
				.toDegrees(man.lon);
		boolean squareCurve = man.flags.contains(Rows.FLAGS.FLG_SQUARE_CURVE);
		boolean invertY = man.flags.contains(Rows.FLAGS.FLG_CURVE_RIGHT);
		Vector<double[]> offsetPoints = calcRowsPoints(man.width,
				man.length, man.hstep, man.alternation / 100.0,
				man.coff, squareCurve, man.bearing,
				man.cross_angle, invertY);

		Vector<double[]> ret = new Vector<double[]>();
		for (double p[] : offsetPoints)
			ret.add(WGS84Utilities.WGS84displace(refLat, refLon, 0, p[0], p[1],
					0));

		return ret;

	}

	/**
	 * Compute all the locations for a given maneuver message
	 * 
	 * @param m
	 *            The maneuver
	 * @return a Collection of locations of the type double[2] = {latitude,
	 *         longitude}. <br/>
	 *         Latitude and Longitude are represented in decimal degrees.
	 */
	public static Collection<double[]> computeLocations(Maneuver m) {
		switch (m.mgid()) {

		case Goto.ID_STATIC:
		case YoYo.ID_STATIC:
		case Loiter.ID_STATIC:
		case CompassCalibration.ID_STATIC:
		case StationKeeping.ID_STATIC:
		case CommsRelay.ID_STATIC:
			return computeSingleLoc(m);
		case Elevator.ID_STATIC:
			if (((Elevator) m).flags.contains(Elevator.FLAGS.FLG_CURR_POS))
				return computeSingleLoc(m);
			else
				return new Vector<double[]>();
		case PopUp.ID_STATIC:
			if (((PopUp) m).flags.contains(PopUp.FLAGS.FLG_CURR_POS))
				return computeSingleLoc(m);
			else
				return new Vector<double[]>();
		case FollowPath.ID_STATIC:
			return computePath((FollowPath) m);
		case FollowTrajectory.ID_STATIC:
			return computePath((FollowTrajectory) m);
		case Rows.ID_STATIC:
			return computePath((Rows) m);
		default:
			return new Vector<double[]>();
		}
	}


	/**
	 * Check if Plan is Cyclic (last maneuver transits to first one)
	 * 
	 * @param plan
	 *            The plan
	 * @return true if last maneuver transits to first one, false otherwise
	 */
	public static boolean isCyclic(PlanSpecification plan){
		String maneuverFirstID = plan.maneuvers.get(0).maneuver_id;
		String maneuverLastID = plan.maneuvers.get(plan.maneuvers.size()-1).maneuver_id;
		
		for (PlanTransition pt : plan.transitions) {
			if (pt.source_man.equals(maneuverLastID)
					&& pt.dest_man.equals(maneuverFirstID)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates an IMC plan corresponding to given maneuver sequence
	 * @param id The id of the generated plan
	 * @param maneuvers The maneuvers contained in the plan
	 * @return IMC plan corresponding to given maneuver sequence
	 */
	public static PlanSpecification createPlan(String id, Maneuver... maneuvers) {
		if (id == null || maneuvers == null || maneuvers.length == 0)
			return null;

		int count = 1;
		PlanSpecification spec = new PlanSpecification();
		spec.plan_id = id;
		spec.start_man_id = "" + count;
		ArrayList<PlanManeuver> pmans = new ArrayList<PlanManeuver>();
		for (Maneuver m : maneuvers) {
			PlanManeuver pm = new PlanManeuver();
			pm.maneuver_id = "" + (count++);
			pm.data = m;
			pmans.add(pm);
		}

		ArrayList<PlanTransition> ptrans = new ArrayList<PlanTransition>();

		for (int i = 2; i <= maneuvers.length; i++) {
			PlanTransition pt = new PlanTransition();
			pt.source_man = "" + (i - 1);
			pt.dest_man = "" + i;
			pt.conditions = "ManeuverIsDone";
			ptrans.add(pt);
		}
			

		spec.maneuvers = pmans;
		spec.transitions = ptrans;

		return spec;
	}
	
	/**
	 * This inner class represents a IMC plan waypoint. <br/>
	 * An IMC maneuver may contain more than one waypoints.
	 * 
	 * @author zp
	 *
	 */
	public static class Waypoint {
		private double latitude, longitude;
		private float altitude, depth, height, radius, time, speed;

		public enum TYPE {
			// Go directly to the waypoint
			REGULAR,
			// Move around the waypoint clockwise
			LOITER_CW,
			// Move around the waypoint counter-clockwise
			LOITER_CCW,
			// Stop at the waypoint
			STATION_KEEP,
			// Other waypoint behavior
			OTHER
		}

		private TYPE type = TYPE.OTHER;

		/**
		 * @return the latitude in degrees of the waypoint
		 */
		public double getLatitude() {
			return latitude;
		}

		/**
		 * @param latitude
		 *            the latitude in degrees of the waypoint
		 */
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}

		/**
		 * @return the longitude in degrees of the waypoint
		 */
		public double getLongitude() {
			return longitude;
		}

		/**
		 * @param longitude
		 *            the longitude in degrees of the waypoint
		 */
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		/**
		 * @return the altitude in meters or {@link java.lang.Float#NaN} if not
		 *         set
		 */
		public float getAltitude() {

			return altitude;
		}

		/**
		 * @param altitude
		 *            the altitude in meters or {@link java.lang.Float#NaN} to
		 *            unset
		 */
		public void setAltitude(float altitude) {
			this.altitude = altitude;
		}

		/**
		 * @return the depth in meters or {@link java.lang.Float#NaN} if not set
		 */
		public float getDepth() {
			return depth;
		}

		/**
		 * @param depth
		 *            the depth in meters or {@link java.lang.Float#NaN} to
		 *            unset
		 */
		public void setDepth(float depth) {
			this.depth = depth;
		}

		/**
		 * @return the WGS84 height in meters or {@link java.lang.Float#NaN} if
		 *         not set
		 */
		public float getHeight() {
			return height;
		}

		/**
		 * @param height
		 *            the WGS84 height in meters or {@link java.lang.Float#NaN}
		 *            to unset
		 */
		public void setHeight(float height) {
			this.height = height;
		}

		/**
		 * @return the radius the radius in meters or 0 if not applicable
		 */
		public float getRadius() {
			return radius;
		}

		/**
		 * @param radius
		 *            the radius in meters or 0 if not applicable
		 */
		public void setRadius(float radius) {
			this.radius = radius;
		}

		/**
		 * @return the time in seconds to stay at this waypoint
		 */
		public float getTime() {
			return time;
		}

		/**
		 * @param time
		 *            time in seconds to stay at this waypoint
		 */
		public void setTime(float time) {
			this.time = time;
		}

		/**
		 * @return this waypoint type
		 */
		public TYPE getType() {
			return type;
		}

		/**
		 * @param type
		 *            the type of waypoint to set
		 */
		public void setType(TYPE type) {
			this.type = type;
		}

		/**
		 * @return the speed in meters per second
		 */
		public float getSpeed() {
			return speed;
		}

		/**
		 * @param speed the speed to set in meters per second
		 */
		public void setSpeed(float speed) {
			this.speed = speed;
		}

		public Waypoint copy() {
			Waypoint copy = new Waypoint();
			copy.setType(getType());
			copy.setLatitude(getLatitude());
			copy.setLongitude(getLongitude());
			copy.setAltitude(getAltitude());
			copy.setDepth(getDepth());
			copy.setHeight(getHeight());
			copy.setRadius(getRadius());
			copy.setTime(getTime());
			return copy;
		}
	}
}
