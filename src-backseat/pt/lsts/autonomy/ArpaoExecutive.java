package pt.lsts.autonomy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Properties;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.AlignmentState;
import pt.lsts.imc4j.msg.AlignmentState.STATE;
import pt.lsts.imc4j.msg.CompassCalibration;
import pt.lsts.imc4j.msg.CompassCalibration.DIRECTION;
import pt.lsts.imc4j.msg.EntityParameter;
import pt.lsts.imc4j.msg.EntityParameters;
import pt.lsts.imc4j.msg.Goto;
import pt.lsts.imc4j.msg.GpsFix;
import pt.lsts.imc4j.msg.GpsFix.VALIDITY;
import pt.lsts.imc4j.msg.PlanControlState;
import pt.lsts.imc4j.msg.PlanControlState.LAST_OUTCOME;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.PopUp;
import pt.lsts.imc4j.msg.PopUp.FLAGS;
import pt.lsts.imc4j.msg.QueryEntityParameters;
import pt.lsts.imc4j.msg.SetEntityParameters;
import pt.lsts.imc4j.msg.Sms;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

public class ArpaoExecutive extends MissionExecutive {

	@Parameter(description = "Sequence of plans to execute after the vehicle is ready")
	public String[] plans = new String[] { "plan1", "plan2" };

	@Parameter(description = "DUNE hostname")
	public String host = "127.0.0.1";

	@Parameter(description = "DUNE TCP port")
	public int port = 6003;

	@Parameter(description = "Length of IMU alignment track")
	public double imu_align_length = 250;

	@Parameter(description = "Bearing of IMU alignment track")
	public double imu_align_bearing = -110;

	@Parameter(description = "If set ot true, the vehicle will align IMU prior to mission execution")
	public boolean align_imu = true;

	@Parameter(description = "If set to true, the vehicle will calibrate the compass prior to mission execution")
	public boolean calibrate_compass = true;

	@Parameter(description = "Time, in minutes, to spend calibrating the compass")
	public int compass_calib_mins = 15;
	
	@Parameter(description = "GSM Number where to send reports. Leave empty to use emergency number.")
	public String gsm_number = "+351914785889";
	

	long time = 0;
	String plan = null;
	int plan_index = 0;
	private String emergencyNumber = null;
	
	public ArpaoExecutive() {
		state = this::init;
	}

	public State init() {

		if (!knowsEmergencyNumber()) {
			QueryEntityParameters query = new QueryEntityParameters();
			query.name = "Emergency Monitor";
			query.scope = "global";
			query.visibility = "user";
			try {
				send(query);	
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			print("Waiting to get GSM emergency number...");
			return this::init;
		}
		
		if (!atSurface()) {
			print("Waiting for the vehicle to be at surface...");
			return this::init;
		}

		if (!gps()) {
			print("Waiting for GPS...");
			return this::init;
		}
		
		if (!ready()) {
			print("Waiting for the vehicle to be ready...");
			return this::init;
		}
		
		if (calibrate_compass) {
			PlanSpecification spec = ccalib();
			plan = spec.plan_id;
			time = System.currentTimeMillis();
			exec(spec);
			return compass_calib();
		} 
		else if (align_imu) {
			PlanSpecification spec = imu();
			plan = spec.plan_id;
			time = System.currentTimeMillis();
			exec(spec);
			return this::imu_align;
		} 
		else {
			plan = "";
			time = System.currentTimeMillis();
			return this::plan_exec;
		}
	}
	
	protected void sendMessage(String message) {
		Sms sms = new Sms();
		sms.contents = message;
		sms.number = emergencyNumber;
		sms.timeout = 60;
		
		try {
			send(sms);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Consume
	protected void on(EntityParameters params) {
		for (EntityParameter p : params.params) {
			if (p.name.equals("SMS Recipient Number"))
				emergencyNumber = p.value;
		}
	}

	public State compass_calib() {
		print("compass_calib");

		if (System.currentTimeMillis() - time < 5000)
			return this::compass_calib;

		PlanControlState pcs = get(PlanControlState.class);

		if (pcs != null && pcs.plan_id.equals(plan) && ready()) {
			if (pcs.last_outcome == LAST_OUTCOME.LPO_SUCCESS) {

				if (align_imu) {
					PlanSpecification spec = imu();
					plan = spec.plan_id;
					time = System.currentTimeMillis();
					exec(spec);
					return this::imu_align;
				} else {
					plan = "";
					time = System.currentTimeMillis();
					return this::plan_exec;
				}
			} else
				return this::init;
		}

		return this::compass_calib;
	}

	public State imu_align() {
		print("imu_align");
		if (System.currentTimeMillis() - time < 5000)
			return this::imu_align;

		if (imu_aligned()) {
			stopPlan();
			time = System.currentTimeMillis();
			plan = "";
			return this::plan_exec;
		}

		if (ready()) {
			PlanSpecification spec = imu();
			plan = spec.plan_id;
			time = System.currentTimeMillis();
			exec(spec);
			return this::imu_align;
		}

		return this::imu_align;
	}

	public State plan_exec() {
		print("plan_exec");
		if (System.currentTimeMillis() - time < 5000)
			return this::plan_exec;

		if (ready()) {
			PlanControlState pcs = get(PlanControlState.class);
			if (pcs.plan_id.equals(plan) && pcs.last_outcome.equals(LAST_OUTCOME.LPO_SUCCESS))
				plan_index++;
			if (plan_index >= plans.length) {
				System.out.println("Finished!");
				return null;
			} else {
				plan = plans[plan_index];
				startPlan(plan);

				time = System.currentTimeMillis();
			}
		}
		return this::plan_exec;
	}

	public boolean knowsEmergencyNumber() {
		if (!gsm_number.isEmpty())
			emergencyNumber = gsm_number;
		
		return emergencyNumber != null;
	}
	
	public boolean gps() {
		GpsFix fix = get(GpsFix.class);
		return fix != null && fix.validity.contains(VALIDITY.GFV_VALID_POS);
	}

	public boolean imu_aligned() {
		AlignmentState state = get(AlignmentState.class);
		return state != null && state.state == STATE.AS_ALIGNED;
	}

	public PlanSpecification ccalib() {
		double[] pos = position();

		if (pos == null)
			return null;

		PopUp popup = new PopUp();
		popup.lat = Math.toRadians(pos[0]);
		popup.lon = Math.toRadians(pos[1]);
		popup.speed = 1;
		popup.speed_units = SpeedUnits.METERS_PS;
		popup.flags.add(FLAGS.FLG_CURR_POS);
		popup.duration = 30;
		popup.z = 0;
		popup.z_units = ZUnits.DEPTH;

		CompassCalibration ccalib = new CompassCalibration();
		ccalib.lat = Math.toRadians(pos[0]);
		ccalib.lon = Math.toRadians(pos[1]);
		ccalib.speed = 1;
		ccalib.speed_units = SpeedUnits.METERS_PS;
		ccalib.direction = DIRECTION.LD_CLOCKW;
		ccalib.amplitude = 0;
		ccalib.z = 0;
		ccalib.z_units = ZUnits.DEPTH;
		ccalib.duration = compass_calib_mins * 60;
		ccalib.radius = 15;

		return spec(popup, ccalib);
	}

	public PlanSpecification imu() {
		double[] pos = position();

		if (pos == null)
			return null;

		PopUp popup = new PopUp();
		popup.lat = Math.toRadians(pos[0]);
		popup.lon = Math.toRadians(pos[1]);
		popup.speed = 1;
		popup.speed_units = SpeedUnits.METERS_PS;
		popup.flags.add(FLAGS.FLG_CURR_POS);
		popup.duration = 30;
		popup.z = 0;
		popup.z_units = ZUnits.DEPTH;

		double offsetX = Math.cos(Math.toRadians(imu_align_bearing)) * 40;
		double offsetY = Math.cos(Math.toRadians(imu_align_bearing)) * 40;

		double[] loc1 = WGS84Utilities.WGS84displace(pos[0], pos[1], 0, offsetX, offsetY, 0);
		Goto man1 = new Goto();
		man1.lat = Math.toRadians(loc1[0]);
		man1.lon = Math.toRadians(loc1[1]);
		man1.speed = 1;
		man1.speed_units = SpeedUnits.METERS_PS;
		man1.z = 0;
		man1.z_units = ZUnits.DEPTH;

		offsetX = Math.cos(Math.toRadians(imu_align_bearing)) * imu_align_length;
		offsetY = Math.cos(Math.toRadians(imu_align_bearing)) * imu_align_length;

		double[] loc2 = WGS84Utilities.WGS84displace(pos[0], pos[1], 0, offsetX, offsetY, 0);
		Goto man2 = new Goto();
		man2.lat = Math.toRadians(loc2[0]);
		man2.lon = Math.toRadians(loc2[1]);
		man2.speed = 1;
		man2.speed_units = SpeedUnits.METERS_PS;
		man2.z = 0;
		man2.z_units = ZUnits.DEPTH;

		Goto man3 = new Goto();
		man3.lat = Math.toRadians(pos[0]);
		man3.lon = Math.toRadians(pos[1]);
		man3.speed = 1;
		man3.speed_units = SpeedUnits.METERS_PS;
		man3.z = 0;
		man3.z_units = ZUnits.DEPTH;

		PlanSpecification spec = spec(popup, man1, man2, man3);
		// Activate IMU on second goto
		SetEntityParameters params = new SetEntityParameters();
		params.name = "IMU";
		EntityParameter param = new EntityParameter();
		param.name = "Active";
		param.value = "true";
		params.params.add(param);
		spec.maneuvers.get(2).start_actions.add(params);

		return spec;
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage: java -jar ArpaoExec.jar <FILE>");
			System.exit(1);
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			ArpaoExecutive tmp = new ArpaoExecutive();
			writer.write("#Arpao Executive Settings\n\n");
			for (Field f : tmp.getClass().getDeclaredFields()) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					Object value = f.get(tmp);
					if (value instanceof String[]) {
						value = String.join(", ", ((String[]) value));
					}
					writer.write("#" + p.description() + "\n");
					writer.write(f.getName() + "=" + value + "\n\n");
				}
			}
			System.out.println("Wrote default properties to " + file.getAbsolutePath());
			writer.close();
			System.exit(0);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(file));

		ArpaoExecutive executive = PojoConfig.create(ArpaoExecutive.class, props);

		System.out.println("Arpao Executive started with settings:");
		for (Field f : executive.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				Object value = f.get(executive);
				if (value instanceof String[])
					value = String.join(", ", ((String[]) value));
				System.out.println(f.getName() + "=" + value);
			}
		}
		System.out.println();

		executive.connect(executive.host, executive.port);
		executive.join();
		executive.init();
	}
}
