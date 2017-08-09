package pt.lsts.backseat.drip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.OptionalDouble;
import java.util.Properties;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.PlanControlState;
import pt.lsts.imc4j.msg.ReportControl;
import pt.lsts.imc4j.msg.Salinity;
import pt.lsts.imc4j.msg.Sms;
import pt.lsts.imc4j.msg.VehicleMedium;
import pt.lsts.imc4j.msg.VehicleMedium.MEDIUM;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

public class RiverPlumeTracker extends TimedFSM {

	@Parameter(description = "Latitude, in degrees, of river mouth")
	double river_lat = 41.145289;

	@Parameter(description = "Longitude, in degrees, of river mouth")
	double river_lon = -8.675311;

	@Parameter(description = "Minimum depth, in meters, for yoyo profiles")
	double min_depth = 0.5;

	@Parameter(description = "Maximum depth, in meters, for yoyo profiles")
	double max_depth = 5.5;

	@Parameter(description = "Speed to travel at during yoyo profiles")
	double yoyo_speed = 1300;
	
	@Parameter(description = "Speed units to use (RPM, m/s)")
	String speed_units = "RPM";

	@Parameter(description = "Number of yoyos to perform on each side of the plume")
	int yoyo_count = 5;

	@Parameter(description = "Start angle, in degrees")
	double start_ang = -180;

	@Parameter(description = "End angle, in degrees")
	double end_ang = -45;

	@Parameter(description = "Variation, in degrees, between survey angles")
	double angle_inc = 10;

	@Parameter(description = "Minimum distance from river mouth")
	double min_dist = 200;

	@Parameter(description = "Maximum distance from river mouth")
	double max_dist = 3000;

	@Parameter(description = "Salinity Threshold")
	double salinity = 30.0;
	
	@Parameter(description = "Number of salinity values to average")
	int salinity_count = 2;
	
	@Parameter(description = "Use Simulated Plume")
	boolean simulated_plume = false;
	
	@Parameter(description = "Distance of simulated plume")
	double plume_dist = 1000;	

	@Parameter(description = "Seconds to idle at each vertex")
	int wait_secs = 60;

	@Parameter(description = "DUNE Host Address")
	String host_addr = "127.0.0.1";

	@Parameter(description = "DUNE Host Port (TCP)")
	int host_port = 6003;
	
	@Parameter(description = "Minutes before termination")
	int mins_timeout = 60;
	
	@Parameter(description = "DUNE plan to execute right after termination")
	String end_plan = "rendezvous";
	
	@Parameter(description = "Maximum time underwater")
	int mins_underwater = 15;
	
	@Parameter(description = "Number where to send reports")
	String sms_recipient = "";
	
	int num_yoyos;
	double angle;
	int count_secs;
	boolean going_in;
	ArrayList<Salinity> salinity_data = new ArrayList<>();	
	int secs_underwater = 0;
	
	public RiverPlumeTracker() {
		state = this::init;
	}

	public FSMState init(FollowRefState state) {
		
		if (get(PlanControlState.class).plan_id.equals("back_seat"))
			print("Back seat driver is started");
		else
			return this::init;
		
		angle = start_ang;
		deadline = new Date(System.currentTimeMillis() + mins_timeout * 60 * 1000);
		if (!end_plan.isEmpty()) {
			endPlan = end_plan;
			System.out.println("Will terminate by "+deadline+" and execute '"+end_plan+"'");
		}
		else
			System.out.println("Will terminate by "+deadline);	
		
		return this::wait;
	}
	
	@Override
	public void connect() throws Exception {
		connect(host_addr, host_port);
	}
	
	@Consume
	public void on(Salinity salinity) {
		synchronized (salinity_data) {
			if (salinity_data.size() >= salinity_count)
				salinity_data.remove(0);
			salinity_data.add(salinity);
		}
	}

	public boolean isInsidePlume() {
		if (simulated_plume)
			return insideSimulatedPlume();		
		else {
			OptionalDouble val;
			synchronized (salinity_data) {
				val = salinity_data.stream().mapToDouble(s -> s.value).average();
			}
			boolean inside = false;
			if (val.isPresent())
				inside = val.getAsDouble() < salinity;
			print("Measured salinity: "+val+", inside plume: "+inside);
			return inside;
		}
	}
	
	public double salinity() {
		OptionalDouble val;
		synchronized (salinity_data) {
			val = salinity_data.stream().mapToDouble(s -> s.value).average();
		}
		return val.orElse(0);
	}
	
	public boolean insideSimulatedPlume() {
		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		boolean inside = WGS84Utilities.distance(pos[0], pos[1], river_lat, river_lon) < plume_dist;
		print("Inside Simulated Plume? " + inside);
		return inside;
	}

	public FSMState go_out(FollowRefState ref) {
		if ((angle_inc > 0 && angle >= end_ang) || (angle_inc < 0 && angle <= end_ang) ) {
			print("Finished!");
			return null;
		}
		print("Going out. Angle: " + angle);
		num_yoyos = 0;
		double angRads = Math.toRadians(angle);
		double offsetX = Math.cos(angRads) * max_dist;
		double offsetY = Math.sin(angRads) * max_dist;
		double[] pos = WGS84Utilities.WGS84displace(river_lat, river_lon, 0, offsetX, offsetY, 0);
		setLocation(pos[0], pos[1]);
		setDepth(max_depth);
		if (speed_units.equalsIgnoreCase("rpm"))
			setSpeed(yoyo_speed, SpeedUnits.RPM);
		else
			setSpeed(yoyo_speed, SpeedUnits.METERS_PS);
		
		return this::descend;
	}

	public FSMState descend(FollowRefState ref) {
		setDepth(max_depth);
		secs_underwater++;
		
		if (arrivedXY()) {
			print("Missed the plume!");
			going_in = !going_in;
			return this::wait;
		}
		if (arrivedZ()) {
			print("Now ascending.");
			return this::ascend;
		}
		else
			return this::descend;
	}

	public FSMState ascend(FollowRefState ref) {
		setDepth(min_depth);
		secs_underwater++;
		
		if (secs_underwater / 60 >= mins_underwater) {
			print("Periodic surface");
			return this::wait;
		}
		
		if (arrivedXY()) {
			print("Missed the plume!");
			going_in = !going_in;
			return this::wait;
		}
		if (min_depth > 0 && arrivedZ() || !isUnderwater()) {
			boolean inside = isInsidePlume();
			if ((inside && going_in) || (!inside && !going_in)) {
				num_yoyos++;
				print("numYoyos: " + num_yoyos);
				if (num_yoyos == yoyo_count) {
					count_secs = 0;
					going_in = !going_in;
					print("Found the plume, now going at the surface.");
					return this::wait;					
				}
			}
			
			if (secs_underwater / 60 >= mins_underwater) {
				print("Periodic surface");
				return this::wait;
			}			
			else {
				print("Now descending (underwater for "+secs_underwater+" seconds).");
				return this::descend;
			}
			
		} else
			return this::ascend;
	}

	public FSMState communicate(FollowRefState ref) {
		if (count_secs == 20) {
			print("Sending position report");
			EnumSet<ReportControl.COMM_INTERFACE> itfs = EnumSet.of(ReportControl.COMM_INTERFACE.CI_GSM);
			itfs.add(ReportControl.COMM_INTERFACE.CI_SATELLITE);
			sendReport(itfs);
		}
		
		if (count_secs == 40 && !sms_recipient.isEmpty()) {
			
			Sms sms = new Sms();
			sms.timeout = 20;
			sms.contents = String.format("DRIP: %s, salinity: %.1f, angle: %.0f", going_in ? "Going in" : "Going out", salinity(), angle);
			sms.number = sms_recipient;
			try {
				print("Sending DRIP state to "+sms_recipient+" ("+sms.contents+")");
				send(sms);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}		
		if (count_secs >= wait_secs) {
			if (going_in)
				return this::go_in;
			else
				return this::go_out;
		} else {
			count_secs++;
			return this::communicate;
		}		
	}
	
	public FSMState wait(FollowRefState ref) {
		
		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		setLocation(pos[0], pos[1]);
		setDepth(0);
		
		// arrived at surface
		if (get(VehicleMedium.class).medium == MEDIUM.VM_WATER) {
			print("Now at surface, sending report.");
			secs_underwater = 0;
			count_secs = 0;
			return this::communicate; 
		}
		else
			return this::wait;
	}

	public FSMState go_in(FollowRefState ref) {
		angle += angle_inc;
		print("Going in. Angle: " + angle);
		num_yoyos = 0;
		double angRads = Math.toRadians(angle);
		double offsetX = Math.cos(angRads) * min_dist;
		double offsetY = Math.sin(angRads) * min_dist;
		double[] pos = WGS84Utilities.WGS84displace(river_lat, river_lon, 0, offsetX, offsetY, 0);
		setLocation(pos[0], pos[1]);
		num_yoyos = 0;
		setDepth(max_depth);
		if (speed_units.equalsIgnoreCase("rpm"))
			setSpeed(yoyo_speed, SpeedUnits.RPM);
		else
			setSpeed(yoyo_speed, SpeedUnits.METERS_PS);		
		return this::descend;
	}
	
	public static void main(String[] args) throws Exception {
		
		if (args.length != 1) {
			System.err.println("Usage: java -jar Drip.jar <FILE>");
			System.exit(1);
		}
		
		File file = new File(args[0]);
		if (!file.exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			RiverPlumeTracker tmp = new RiverPlumeTracker();
			writer.write("#River Plume Tracker Settings\n\n");
			for (Field f : tmp.getClass().getDeclaredFields()) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					writer.write("#" + p.description()+"\n");
					writer.write(f.getName() + "=" + f.get(tmp)+"\n\n");					
				}
			}
			System.out.println("Wrote default properties to "+file.getAbsolutePath());
			writer.close();
			System.exit(0);			
		}
		
		Properties props = new Properties();
		props.load(new FileInputStream(file));
				
		RiverPlumeTracker tracker = PojoConfig.create(RiverPlumeTracker.class, props);
		
		System.out.println("River Plume Tracker started with settings:");
		for (Field f : tracker.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				System.out.println(f.getName() + "=" + f.get(tracker));
			}
		}
		System.out.println();

		tracker.connect(tracker.host_addr, tracker.host_port);
		tracker.join();
	}
}