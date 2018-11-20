package pt.lsts.backseat.drip;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;

import pt.lsts.autonomy.soi.SoiExecutive;
import pt.lsts.endurance.Plan;
import pt.lsts.endurance.Waypoint;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.ProfileSample;
import pt.lsts.imc4j.msg.SoiCommand;
import pt.lsts.imc4j.msg.SoiCommand.COMMAND;
import pt.lsts.imc4j.msg.SoiCommand.TYPE;
import pt.lsts.imc4j.msg.VerticalProfile;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

public class DripExecutive extends SoiExecutive {

	@Parameter(description = "Latitude, in degrees, of river mouth")
	double river_lat = 41.144789;

	@Parameter(description = "Longitude, in degrees, of river mouth")
	double river_lon = -8.679689;

	@Parameter(description = "Number of yoyos to perform on each side of the plume")
	int yoyo_count = 5;

	@Parameter(description = "Start angle, in degrees")
	double start_ang = -180;

	@Parameter(description = "End angle, in degrees")
	double end_ang = -45;

	@Parameter(description = "Variation, in degrees, between survey angles")
	double angle_inc = 10;

	@Parameter(description = "Minimum distance from river mouth")
	double min_dist = 750;

	@Parameter(description = "Maximum distance from river mouth")
	double max_dist = 15000;

	@Parameter(description = "Use Simulated Plume")
	boolean simulated_plume = false;

	@Parameter(description = "Distance of simulated plume")
	double plume_dist = 1000;
	
	@Parameter(description = "Plume Gradient")
	double plume_gradient = 5;
	
	@Parameter(description = "Plume Threshold")
	double plume_threshold = 30;
	
	
	private final String PLAN_ID = "drip_plan";
	
	int num_yoyos = 0;
	double angle;
	boolean going_in = false;
	boolean init = true;
	
	public DripExecutive() {
		setPlanName(PLAN_ID);
		setDeadline(new Date(System.currentTimeMillis() + timeout * 60 * 1000));
		state = this::idleAtSurface;
	}
	
	@Override
	protected FSMState onIdle() {
		num_yoyos = 0;
		if (init) {
			angle = start_ang;
			init = false;
		}
		print("DRiP is started.");
		if (going_in)
			go_in("initial_transect");
		else
			go_out("initial_transect");
		return this::start_waiting;
		
	}
	
	private void go_out(String planId) {
		//calculate coords
		double angRads = Math.toRadians(angle);
		double offsetX = Math.cos(angRads) * max_dist;
		double offsetY = Math.sin(angRads) * max_dist;
		double[] target = WGS84Utilities.WGS84displace(river_lat, river_lon, 0, offsetX, offsetY, 0);
		setTarget(planId,  target[0], target[1]);
		num_yoyos = 0;
	}
	
	private void go_in(String planId) {
		
		double lld[] = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		
		double offsets[] = WGS84Utilities.WGS84displacement(river_lat, river_lon, 0, lld[0], lld[1], 0);
		
		double angRads = Math.atan2(offsets[1], offsets[0]);
		
		//double angRads = Math.toRadians(angle);
		double offsetX = Math.cos(angRads) * min_dist;
		double offsetY = Math.sin(angRads) * min_dist;
		double[] target = WGS84Utilities.WGS84displace(river_lat, river_lon, 0, offsetX, offsetY, 0);
		setTarget(planId, target[0], target[1]);
		num_yoyos = 0;
	}
	
	
	private Plan setTarget(String id, double destLat, double destLon) {
		Plan plan = new Plan(id);
		
		double lld[] = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		Waypoint wpt1 = new Waypoint(0, (float)lld[0], (float)lld[1]);
		Waypoint wpt2 = new Waypoint(1, (float)destLat, (float)destLon);
		
		plan.addWaypoint(wpt1);
		plan.addWaypoint(wpt2);
		
		try {
			SoiCommand newPlan = new SoiCommand();
			newPlan.src = remoteSrc;
			newPlan.command = COMMAND.SOICMD_EXEC;
			newPlan.type = TYPE.SOITYPE_REQUEST;
			newPlan.plan = plan.asImc();
			newPlan.info = "DRiP Plan generated on "+LocalDateTime.now();
			on(newPlan);
			return plan;
		}
		catch (Exception e) {
			printError("Could not generate plan.");
			return null;
		}	
	}
	
	@Override
	protected FSMState onSalinityProfile(VerticalProfile salinity) {
		print("Processing salinity profile");
		return onProfile(salinity);
	}
	
	protected FSMState onProfile(VerticalProfile profile) {
		double lld[] = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		
		if (insidePlume(profile) && going_in) {
			num_yoyos++;
			print("Inside the plume ("+num_yoyos+" yoyos)");
		}
		else if (!insidePlume(profile) && !going_in) {
			num_yoyos++;
			print("Outside the plume ("+num_yoyos+" yoyos)");
		}
		
		if (num_yoyos >= yoyo_count) {
			going_in = !going_in;
			print("Reversing the direction");
			if(going_in)
				go_in("got_plume");
			else {
				angle += angle_inc;
				print("Going with angle "+angle);
				go_out("got_plume");
			}
			return this::start_waiting;
		}
		
		double distToMouth = WGS84Utilities.distance(lld[0], lld[1], river_lat, river_lon);
		if (distToMouth > max_dist && !going_in) {
			go_in("no_plume");
			return this::start_waiting;
		}
		else if (distToMouth < min_dist && going_in) {
			angle += angle_inc;
			print("Going with angle "+angle);
			go_out("no_plume");
			return this::start_waiting;
		}
		
		return null;
	}
	
	@Override
	protected FSMState onTemperatureProfile(VerticalProfile temp) {
		if (simulated_plume) {
			print("Processing simulated (temperature) profile");
			return onProfile(temp);
		}
		return null;
	}
	
	private boolean insidePlume(VerticalProfile salinity) {
		if (simulated_plume) {
			double lld[] = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
			double distToMouth = WGS84Utilities.distance(lld[0], lld[1], river_lat, river_lon);
			
			System.out.println("Inside? "+distToMouth+" < "+plume_dist);
			return distToMouth < plume_dist;
		}
		
		if (salinity.samples.isEmpty())
			return false;
		
		double min = salinity.samples.get(0).avg;
		double max = min;
		
		for (ProfileSample sal : salinity.samples) {
			min = Math.min(min, sal.avg);
			max = Math.max(max, sal.avg);
		}
		
		boolean inside = false;
		
		if (plume_gradient > 0) {
			inside = (max-min) > plume_gradient;
			print("Gradient difference is "+(max-min)+", inside: "+inside);
		}
		else {
			inside = min < plume_threshold;
			print("Min salinity is "+min+", inside: "+inside);
		}
		
		return inside;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: java -jar Drip.jar <FILE>");
			System.exit(1);
		}

		CONFIG_FILE = new File(args[0]);
		if (!CONFIG_FILE.exists()) {
			new DripExecutive().saveConfig(CONFIG_FILE);
			System.out.println("Wrote default properties to " + CONFIG_FILE.getAbsolutePath());
			System.exit(0);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(CONFIG_FILE));

		DripExecutive tracker = PojoConfig.create(DripExecutive.class, props);

		System.out.println("Executive started with settings:");
		for (Field f : PojoConfig.loadFields(tracker)) {
			f.setAccessible(true);
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				System.out.println(f.getName() + "=" + f.get(tracker));
			}
		}
		System.out.println();

		tracker.connect(tracker.hAddr, tracker.hPort);
		tracker.join();
	}
}
