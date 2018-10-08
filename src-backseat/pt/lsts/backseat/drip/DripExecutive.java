package pt.lsts.backseat.drip;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

import pt.lsts.autonomy.soi.SoiExecutive;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.msg.VerticalProfile;
import pt.lsts.imc4j.util.PojoConfig;

public class DripExecutive extends SoiExecutive {

	@Parameter(description = "Latitude, in degrees, of river mouth")
	double river_lat = 41.145289;

	@Parameter(description = "Longitude, in degrees, of river mouth")
	double river_lon = -8.675311;

	@Parameter(description = "Maximum depth, in meters, for yoyo profiles")
	double max_depth = 5.5;

	@Parameter(description = "Speed to use (m/s)")
	double speed = 1.3;

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

	@Parameter(description = "Use Simulated Plume")
	boolean simulated_plume = false;

	@Parameter(description = "Distance of simulated plume")
	double plume_dist = 1000;

	@Parameter(description = "Seconds to idle at each vertex")
	int wait_secs = 60;

	@Parameter(description = "DUNE Host Address")
	String host_addr = "127.0.0.1";

	@Parameter(description = "DUNE Host Port (TCP)")
	int host_port = 6006;

	@Parameter(description = "Minutes before termination")
	int mins_timeout = 60;

	@Parameter(description = "Maximum time underwater")
	int mins_underwater = 15;

	@Parameter(description = "Number where to send reports")
	String sms_recipient = "";

	private final String PLAN_ID = "drip_plan";
	
	boolean init = false;
	
	int num_yoyos;
	double angle;
	int count_secs;
	boolean going_in;
	
	public DripExecutive() {
		setPlanName(PLAN_ID);
		setDeadline(new Date(System.currentTimeMillis() + mins_timeout * 60 * 1000));
		state = this::idleAtSurface;
	}
	
	@Override
	protected void onIdle() {
		print("Drip idle...");
		if (!init) {
			num_yoyos = 0;
			angle = start_ang;
			count_secs = 0;
			going_in = false;
			init = true;
			print("DRIP initialized with provided settings.");
		}
	}
	
	@Override
	protected void onSalinityProfile(VerticalProfile salinity) {
		super.onSalinityProfile(salinity);
	}
	
	@Override
	protected void onTemperatureProfile(VerticalProfile salinity) {
		print("Processing temperature profile");
	}
	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: java -jar Drip.jar <FILE>");
			System.exit(1);
		}

		CONFIG_FILE = new File(args[0]);
		if (!CONFIG_FILE.exists()) {
			new SoiExecutive().saveConfig(CONFIG_FILE);
			System.out.println("Wrote default properties to " + CONFIG_FILE.getAbsolutePath());
			System.exit(0);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(CONFIG_FILE));

		DripExecutive tracker = PojoConfig.create(DripExecutive.class, props);

		System.out.println("Executive started with settings:");
		for (Field f : tracker.getClass().getDeclaredFields()) {
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
