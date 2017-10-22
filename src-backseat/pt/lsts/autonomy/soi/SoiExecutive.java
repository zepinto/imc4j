package pt.lsts.autonomy.soi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.EnumSet;
import java.util.Properties;
import java.util.concurrent.Future;

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
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.ReportControl;
import pt.lsts.imc4j.msg.SoiCommand;
import pt.lsts.imc4j.msg.SoiState;
import pt.lsts.imc4j.msg.SoiState.STATE;
import pt.lsts.imc4j.msg.VehicleMedium;
import pt.lsts.imc4j.msg.VehicleMedium.MEDIUM;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

public class SoiExecutive extends TimedFSM {

	@Parameter(description = "Nominal Speed")
	double speed = 1;

	@Parameter(description = "Maximum Depth")
	double max_depth = 10;

	@Parameter(description = "Minimum Depth")
	double min_depth = 0.0;

	@Parameter(description = "Maximum Speed")
	double max_speed = 1.5;

	@Parameter(description = "Minimum Speed")
	double min_speed = 0.7;

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
	boolean cycle = false;

	private Plan plan = new Plan("idle");
	private PlanSpecification spec = null;
	private int secs_underwater = 0, count_secs = 0;
	private int wpt_index = 0;
	private Future<Void> ongoingIridium = null;

	public SoiExecutive() {
		setPlanName(soi_plan_id);
		setDeadline(new Date(System.currentTimeMillis() + mins_timeout * 60 * 1000));
		state = this::init;
	}

	@Consume
	public void on(PlanDB planDb) {
		if (planDb.op == PlanDB.OP.DBOP_DEL && planDb.type == TYPE.DBT_REQUEST) {
			if (planDb.plan_id.equals(soi_plan_id)) {
				print("Stop execution (SOI plan removed)");
				spec = null;
				plan = null;
				wpt_index = 0;
				state = this::idleAtSurface;
			}
		}
		if (planDb.op == PlanDB.OP.DBOP_SET && planDb.type == TYPE.DBT_REQUEST) {
			if (planDb.plan_id.equals(soi_plan_id)) {
				spec = (PlanSpecification) planDb.arg;
				plan = Plan.parse(spec);
				EstimatedState s = get(EstimatedState.class);
				if (s != null) {
					double[] pos = WGS84Utilities.toLatLonDepth(s);
					plan.scheduleWaypoints(System.currentTimeMillis(), pos[0], pos[1], speed);
				} else
					plan.scheduleWaypoints(System.currentTimeMillis(), speed);

				print("Received soi plan:");
				print("" + plan);
				wpt_index = 0;
				state = this::start_waiting;
			}
		}
	}

	@Consume
	public void on(SoiCommand cmd) {
		if (cmd.type != SoiCommand.TYPE.SOITYPE_REQUEST)
			return;
		System.out.println("SoiCommand!\n" + cmd);
		SoiCommand reply = new SoiCommand();
		reply.command = cmd.command;
		reply.type = SoiCommand.TYPE.SOITYPE_ERROR;
		reply.src = remoteSrc;
		reply.dst = cmd.src;
		reply.dst_ent = cmd.src_ent;

		switch (cmd.command) {

		case SOICMD_EXEC:
			print("CMD: Exec plan!");
			if (cmd.plan == null) {
				plan = null;
				state = this::idleAtSurface;
			}
			else {
				plan = Plan.parse(cmd.plan);

				if (!plan.scheduledInTheFuture()) {
					EstimatedState s = get(EstimatedState.class);
					if (s != null) {
						double[] pos = WGS84Utilities.toLatLonDepth(s);
						plan.scheduleWaypoints(System.currentTimeMillis(), pos[0], pos[1], speed);
					} else
						plan.scheduleWaypoints(System.currentTimeMillis(), speed);
				}					
				wpt_index = 0;
				print("Start executing this plan:");
				print("" + plan);
				state = this::start_waiting;				
			}
			reply.type = SoiCommand.TYPE.SOITYPE_SUCCESS;
			break;

		case SOICMD_GET_PARAMS:
			print("CMD: Get Params!");
			for (Field f : getClass().getDeclaredFields()) {
				f.setAccessible(true);
				Parameter p = f.getAnnotation(Parameter.class);
				if (p == null)
					continue;
				String name = f.getName();
				try {
					reply.settings.set(name, f.get(this));
				} catch (Exception e) {

				}
			}
			reply.type = SoiCommand.TYPE.SOITYPE_SUCCESS;
			break;

		case SOICMD_SET_PARAMS:
			print("CMD: Set Params!");
			try {
				for (String key : cmd.settings.keys())
					PojoConfig.setProperty(this, key, cmd.settings.get(key));
				reply.type = SoiCommand.TYPE.SOITYPE_SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;

		case SOICMD_STOP:
			print("CMD: Stop execution!");
			setPaused(true);
			state = this::start_waiting;
			reply.type = SoiCommand.TYPE.SOITYPE_SUCCESS;
			break;

		case SOICMD_GET_PLAN:
			print("CMD: Get plan!");
			reply.plan = plan.asImc();
			reply.type = SoiCommand.TYPE.SOITYPE_SUCCESS;
			break;

		case SOICMD_RESUME:
			print("CMD: Resume execution!");
			if (paused) {
				setPaused(false);
				state = this::start_waiting;
				return;
			}
			break;
		default:
			break;
		}

		try {
			send(reply);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// FIXME make sure we wait for reply transmission...
		ongoingIridium = sendViaIridium(reply, 60);
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

	private SoiState createSoiState() {
		SoiState state = new SoiState();
		if (plan != null && plan.waypoint(wpt_index) != null) {
			state.state = STATE.SOISTATE_EXEC;
			state.plan_id = plan.checksum();
		} else {
			state.state = STATE.SOISTATE_IDLE;
			state.plan_id = 0;
		}

		state.wpt_id = wpt_index;
		state.settings_chk = 0;

		return state;

	}

	private String createReport() {
		EstimatedState state = get(EstimatedState.class);
		FuelLevel flevel = get(FuelLevel.class);
		String plan_chk = "-";
		if (plan != null)
			plan_chk = "" + plan.checksum();

		String pos = "?,?";
		if (state != null) {
			double[] loc = WGS84Utilities.toLatLonDepth(state);
			pos = (float) loc[0] + "," + (float) loc[1];
		}
		String fuel = flevel == null ? "?" : "" + (int) flevel.value;
		return remoteSrc + "," + pos + "," + plan_chk + "," + wpt_index + "," + fuel;
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
		return this::idle;
	}

	public FSMState exec(FollowRefState state) {

		if (plan == null)
			return this::idleAtSurface;

		Waypoint wpt = plan.waypoint(wpt_index);

		if (wpt == null) {
			print("Finished executing plan.");
			if (cycle) {
				wpt_index = 0;
				EstimatedState s = get(EstimatedState.class);
				if (s != null) {
					double[] pos = WGS84Utilities.toLatLonDepth(s);
					plan.scheduleWaypoints(System.currentTimeMillis(), pos[0], pos[1], speed);
				} else
					plan.scheduleWaypoints(System.currentTimeMillis(), speed);
				return this::exec;
			} else
				return this::idleAtSurface;
		}

		print("Executing wpt " + wpt_index);
		setLocation(wpt.getLatitude(), wpt.getLongitude());
		setSpeed();

		return this::descend;
	}

	public void setSpeed() {
		Waypoint wpt = plan.waypoint(wpt_index);
		double speed = this.speed;

		if (wpt == null)
			speed = 0;

		if (wpt.getArrivalTime() != null) {
			double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
			double dist = WGS84Utilities.distance(wpt.getLatitude(), wpt.getLongitude(), pos[0], pos[1]);
			double secs = (wpt.getArrivalTime().getTime() - System.currentTimeMillis()) / 1000.0;

			if (secs < 0) {
				speed = max_speed;
			} else {
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
			print("Arrived at waypoint " + wpt_index);
			wpt_index++;
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
			wpt_index++;
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

	public FSMState communicate(FollowRefState ref) {

		// Send "DUNE" report
		if (count_secs == 0) {
			EnumSet<ReportControl.COMM_INTERFACE> itfs = EnumSet.of(ReportControl.COMM_INTERFACE.CI_GSM);
			itfs.add(ReportControl.COMM_INTERFACE.CI_SATELLITE);
			sendReport(itfs);
			sendViaSms(createReport(), wait_secs - count_secs - 1);
			ongoingIridium = sendViaIridium(createSoiState(), wait_secs - count_secs - 1);
		}

		if (ongoingIridium != null && ongoingIridium.isDone()) {
			try {
				ongoingIridium.get();
				print("Report was sent via iridium!");
				return this::exec;
			} catch (Exception e) {
				print("Error transmitting over Iridium: " + e.getMessage());				
			}
			ongoingIridium = null;
		}		

		if (count_secs >= wait_secs / 2 && ongoingIridium == null) {
			return this::exec;
		}
		else if (count_secs >= wait_secs) {
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
