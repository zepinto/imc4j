package pt.lsts.autonomy.soi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.EnumSet;
import java.util.Properties;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.msg.EntityParameter;
import pt.lsts.imc4j.msg.EntityParameters;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.FuelLevel;
import pt.lsts.imc4j.msg.PlanDB;
import pt.lsts.imc4j.msg.PlanDB.TYPE;
import pt.lsts.imc4j.msg.VehicleMedium.MEDIUM;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.ReportControl;
import pt.lsts.imc4j.msg.Sms;
import pt.lsts.imc4j.msg.VehicleMedium;

public class SoiExecutive extends TimedFSM {

	@Parameter(description = "Nominal Speed")
	double speed = 1;

	@Parameter(description = "Maximum Speed")
	double max_speed = 1.5;

	@Parameter(description = "Minimum Speed")
	double min_speed = 0.7;

	@Parameter(description = "Maximum Depth")
	double max_depth = 10;

	@Parameter(description = "Minimum Depth")
	double min_depth = 0.0;

	@Parameter(description = "DUNE Host Address")
	String host_addr = "127.0.0.1";

	@Parameter(description = "DUNE Host Port (TCP)")
	int host_port = 6006;

	@Parameter(description = "Minutes before termination")
	int mins_timeout = 600;

	@Parameter(description = "Maximum time underwater")
	int mins_under = 10;

	@Parameter(description = "Number where to send reports")
	String sms_number = "+351914785889";

	@Parameter(description = "Seconds to idle at each vertex")
	int wait_secs = 60;

	@Parameter(description = "SOI plan identifier")
	String soi_plan_id = "soi_plan";
	
	@Parameter(description = "Cyclic execution")
	boolean cycle = true;
	
	private SoiPlan plan = new SoiPlan("idle");
	private PlanSpecification spec = null;
	private SoiWaypoint currentWaypoint = null;
	private int secs_underwater = 0, count_secs = 0;
	private String waypoint = "-";

	public SoiExecutive() {
		state = this::init;
	}

	@Consume
	public void on(PlanDB planDb) {
		if (planDb.op == PlanDB.OP.DBOP_DEL && planDb.type == TYPE.DBT_REQUEST) {
			if (planDb.plan_id.equals(soi_plan_id)) {
				print("Stop execution (SOI plan removed)");
				spec = null;
				plan = null;
				currentWaypoint = null;
				state = this::idleAtSurface;
			}
		}
		if (planDb.op == PlanDB.OP.DBOP_SET && planDb.type == TYPE.DBT_REQUEST) {
			
			if (planDb.plan_id.equals(soi_plan_id)) {
				spec = (PlanSpecification) planDb.arg;
				currentWaypoint = null;
				state = this::loadPlan;
			}
		}
	}

	@Consume
	public void on(EntityParameters params) {
		if (params.name.equals(getClass().getSimpleName())) {
			for (EntityParameter param : params.params) {
				try {
					PojoConfig.setValue(this, param.name, param.value);
					System.out.println("Set " + param.name + " := " + param.value);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String createReport() {
		EstimatedState state = get(EstimatedState.class);
		FuelLevel flevel = get(FuelLevel.class);
		
		String pos = "?,?";
		if (state != null) {
			double[] loc = WGS84Utilities.toLatLonDepth(state);
			pos = (float)loc[0]+","+(float)loc[1];
		}
		String fuel = flevel == null? "?" : ""+(int)flevel.value;
		return remoteSrc+","+pos+","+waypoint+","+fuel; 
	}
	
	public FSMState init(FollowRefState state) {
		deadline = new Date(System.currentTimeMillis() + mins_timeout * 60 * 1000);
		return this::idleAtSurface;
	}

	public FSMState idleAtSurface(FollowRefState state) {
		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		setLocation(pos[0], pos[1]);
		setDepth(0);
		return this::idle;
	}

	public FSMState idle(FollowRefState state) {
		print("Waiting for plan...");
		waypoint = "-";
		return this::idle;
	}

	public FSMState loadPlan(FollowRefState state) {
		plan = SoiPlan.parse(spec);
		plan.scheduleWaypoints(System.currentTimeMillis(), speed);
		print("Start executing this plan:");
		print(""+plan);
		return this::start_waiting;
	}

	public FSMState exec(FollowRefState state) {

		SoiWaypoint waypoint = currentWaypoint;

		if (waypoint == null)
			waypoint = currentWaypoint = plan.pollWaypoint();

		if (waypoint == null) {
			print("Finished executing plan.");
			if (spec != null && cycle)
				return this::loadPlan;
			else
				return this::idleAtSurface;
		}

		this.waypoint = ""+waypoint.getId();
		print("Executing wpt " + waypoint.getId());

		setLocation(waypoint.getLatitude(), waypoint.getLongitude());
		setSpeed();

		return this::descend;
	}
	
	public void setSpeed() {
		SoiWaypoint waypoint = currentWaypoint;
		double speed = this.speed;

		if (waypoint == null)
			speed = 0;

		if (waypoint.getArrivalTime() != null) {
			double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
			double dist = WGS84Utilities.distance(currentWaypoint.getLatitude(), currentWaypoint.getLongitude(), pos[0],
					pos[1]);
			double secs = (waypoint.getArrivalTime().getTime() - System.currentTimeMillis()) / 1000.0;
			if (secs < 0) {
				speed = max_speed;
			}
			else {
				speed = Math.min(max_speed, dist / secs);
				speed = Math.max(min_speed, speed);	
			}
			
		}
		setSpeed(speed, SpeedUnits.METERS_PS);
	}

	public FSMState descend(FollowRefState ref) {
		setDepth(max_depth);
		secs_underwater++;

		if (arrivedXY()) {
			print("Arrived at waypoint");
			currentWaypoint = null;
			return this::start_waiting;
		}
		if (arrivedZ()) {
			setSpeed();
			if (min_depth < max_depth)
				print("Now ascending.");
			return this::ascend;
		} else
			return this::descend;
	}

	public FSMState ascend(FollowRefState ref) {
		setDepth(min_depth);
		secs_underwater++;

		if (secs_underwater / 60 >= mins_under) {
			print("Periodic surface");
			return this::start_waiting;
		}

		if (arrivedXY()) {
			print("Arrived at waypoint");
			currentWaypoint = null;
			return this::start_waiting;
		}

		if (min_depth > 0 && arrivedZ() || !isUnderwater()) {
			if (secs_underwater / 60 >= mins_under) {
				print("Periodic surface");
				return this::start_waiting;
			} else {
				setSpeed();
				if (max_depth != min_depth)
					print("Now descending (underwater for " + secs_underwater + " seconds).");
				return this::descend;
			}

		} else
			return this::ascend;
	}
	
	public void sendSms(String number, String text, int timeout) {
		Sms sms = new Sms();
		sms.timeout = timeout;
		sms.contents = text;
		sms.number = number;
	}

	public FSMState communicate(FollowRefState ref) {
		if (count_secs == 15) {
			print("Sending position report");
			EnumSet<ReportControl.COMM_INTERFACE> itfs = EnumSet.of(ReportControl.COMM_INTERFACE.CI_GSM);
			itfs.add(ReportControl.COMM_INTERFACE.CI_SATELLITE);
			sendReport(itfs);
		}

		if (count_secs == 30 && !sms_number.isEmpty()) {

			Sms sms = new Sms();
			sms.timeout = wait_secs - count_secs;
			sms.contents = "SOI: "+createReport();
			sms.number = sms_number;
			try {
				print("Sending executive state to " + sms_number + " (" + sms.contents + ")");
				send(sms);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (count_secs >= wait_secs) {
			return this::exec;
		} else {
			count_secs++;
			return this::communicate;
		}
	}

	public FSMState start_waiting(FollowRefState ref) {
		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		setLocation(pos[0], pos[1]);
		setDepth(0);
		setSpeed(speed, SpeedUnits.METERS_PS);

		return this::wait;
	}

	public FSMState wait(FollowRefState ref) {

		// arrived at surface
		if (get(VehicleMedium.class).medium == MEDIUM.VM_WATER) {
			print("Now at surface, sending report.");
			double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
			setLocation(pos[0], pos[1]);			
			secs_underwater = 0;
			count_secs = 0;
			return this::communicate;
		} else
			return this::wait;
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage: java -jar SoiExec.jar <FILE>");
			System.exit(1);
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			SoiExecutive tmp = new SoiExecutive();
			writer.write("#SOI Executive settings\n\n");
			for (Field f : tmp.getClass().getDeclaredFields()) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					writer.write("#" + p.description() + "\n");
					writer.write(f.getName() + "=" + f.get(tmp) + "\n\n");
				}
			}
			System.out.println("Wrote default properties to " + file.getAbsolutePath());
			writer.close();
			System.exit(0);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(file));

		SoiExecutive tracker = PojoConfig.create(SoiExecutive.class, props);

		System.out.println("Executive started with settings:");
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
