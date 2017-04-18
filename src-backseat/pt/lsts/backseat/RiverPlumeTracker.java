package pt.lsts.backseat;

import java.lang.reflect.Field;

import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

public class RiverPlumeTracker extends FSMController {

	@Parameter(description = "Latitude, in degrees, of river mouth")
	double river_lat = 41.185467;

	@Parameter(description = "Longitude, in degrees, of river mouth")
	double river_lon = -8.705522;

	@Parameter(description = "Maximum depth, in meters, for yoyo profiles")
	double min_depth = 0;

	@Parameter(description = "Minimum depth, in meters, for yoyo profiles")
	double max_depth = 10;

	@Parameter(description = "Speed, in m/s, to travel at during yoyo profiles")
	double yoyo_speed = 1.3;

	@Parameter(description = "Number of yoyos to perform on each side of the plume")
	int yoyo_count = 3;

	@Parameter(description = "Start angle, in degrees")
	double start_ang = -155;

	@Parameter(description = "End angle, in degrees")
	double end_ang = -45;

	@Parameter(description = "Variation, in degrees, between survey angles")
	double angle_inc = 10;

	@Parameter(description = "Maximum distance from river mouth")
	double max_dist = 1000;

	@Parameter(description = "Maximum distance from river mouth")
	double min_dist = 200;

	@Parameter(description = "Depth to use for the vertical profiles (0 for no elevator)")
	double elev_depth = 15;

	@Parameter(description = "Seconds to idle at each vertex")
	int wait_secs = 60;

	@Parameter(description = "DUNE Host Address")
	String host_addr = "127.0.0.1";

	@Parameter(description = "DUNE Host Port (TCP)")
	int host_port = 6003;

	int num_yoyos;
	double angle;
	int count_secs;
	boolean going_in;

	public RiverPlumeTracker() {
		state = this::go_out;
	}

	public void init() {
		angle = start_ang;
	}

	public boolean isInsidePlume() {
		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		boolean inside = WGS84Utilities.distance(pos[0], pos[1], river_lat, river_lon) < 300;
		print("insidePlume? " + inside);
		return inside;
	}

	public FSMState go_out(FollowRefState ref) {
		angle += angle_inc;
		if (angle >= end_ang) {
			print("Finished!");
			return null;
		}
		print("Angle: " + angle);
		num_yoyos = 0;
		double angRads = Math.toRadians(angle);
		double offsetX = Math.cos(angRads) * max_dist;
		double offsetY = Math.sin(angRads) * max_dist;
		double[] pos = WGS84Utilities.WGS84displace(river_lat, river_lon, 0, offsetX, offsetY, 0);
		setLocation(pos[0], pos[1]);
		setDepth(max_depth);
		setSpeed(yoyo_speed, SpeedUnits.METERS_PS);
		return this::descend;
	}

	public FSMState descend(FollowRefState ref) {
		setDepth(max_depth);

		if (arrivedXY()) {
			print("Missed the plume!");
			going_in = !going_in;
			if (going_in)
				return this::go_in;
			else
				return this::go_out;
		}
		if (arrivedZ())
			return this::ascend;
		else
			return this::descend;
	}

	public FSMState ascend(FollowRefState ref) {
		setDepth(min_depth);

		if (arrivedXY()) {
			print("Missed the plume!");
			if (going_in)
				return this::go_out;
			else
				return this::go_in;
		}
		if (arrivedZ()) {
			boolean inside = isInsidePlume();
			if ((inside && going_in) || (!inside && !going_in)) {
				num_yoyos++;
				print("numYoyos: " + num_yoyos);
				if (num_yoyos == yoyo_count) {
					count_secs = 0;
					if (elev_depth == 0)
						return this::wait;
					else
						return this::elevator;
				}
			}
			return this::descend;
		} else
			return this::ascend;
	}

	public FSMState elevator(FollowRefState ref) {
		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		setLocation(pos[0], pos[1]);
		setDepth(elev_depth);
		return this::elev_down;
	}

	public FSMState elev_down(FollowRefState ref) {
		if (arrivedZ()) {
			setDepth(0);
			return this::wait;
		}
		return this::elev_down;
	}

	public FSMState wait(FollowRefState ref) {
		if (count_secs >= wait_secs) {
			going_in = !going_in;

			if (going_in)
				return this::go_in;
			else
				return this::go_out;
		} else {
			count_secs++;
			return this::wait;
		}
	}

	public FSMState go_in(FollowRefState ref) {
		num_yoyos = 0;
		double angRads = Math.toRadians(angle);
		double offsetX = Math.cos(angRads) * min_dist;
		double offsetY = Math.sin(angRads) * min_dist;
		double[] pos = WGS84Utilities.WGS84displace(river_lat, river_lon, 0, offsetX, offsetY, 0);
		setLocation(pos[0], pos[1]);
		num_yoyos = 0;
		setDepth(max_depth);
		setSpeed(yoyo_speed, SpeedUnits.METERS_PS);
		return this::descend;
	}

	public static void main(String[] args) throws Exception {
		RiverPlumeTracker tracker = PojoConfig.create(RiverPlumeTracker.class, args);
		tracker.init();

		System.out.println("River Plume Tracker started with settings:");
		for (Field f : tracker.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				System.out.print("  " + f.getName() + "\t= " + f.get(tracker));
				System.out.println("\t#" + p.description());
			}
		}
		System.out.println();

		tracker.connect(tracker.host_addr, tracker.host_port);
		tracker.join();
	}
}