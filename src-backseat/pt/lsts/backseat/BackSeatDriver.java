package pt.lsts.backseat;

import java.util.LinkedHashMap;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.backseat.TcpClient;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.DesiredSpeed;
import pt.lsts.imc4j.msg.DesiredZ;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.FollowReference;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;
import pt.lsts.imc4j.msg.PlanControl;
import pt.lsts.imc4j.msg.PlanControl.OP;
import pt.lsts.imc4j.msg.PlanControl.TYPE;
import pt.lsts.imc4j.msg.PlanControlState;
import pt.lsts.imc4j.msg.PlanControlState.STATE;
import pt.lsts.imc4j.msg.PlanManeuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.Reference;
import pt.lsts.imc4j.msg.Reference.FLAGS;
import pt.lsts.imc4j.msg.VehicleState;
import pt.lsts.imc4j.msg.VehicleState.OP_MODE;

public abstract class BackSeatDriver extends TcpClient {

	protected LinkedHashMap<Integer, Message> state = new LinkedHashMap<>();
	protected long startCommandTime = 0;
	protected boolean executing = false, finished = false;
	protected static final String PLAN_NAME = "back_seat";
	private Reference reference = new Reference();
	
	public void setLocation(double latDegs, double lonDegs) {
		reference.lat = Math.toRadians(latDegs);
		reference.lon = Math.toRadians(lonDegs);
		reference.flags.add(FLAGS.FLAG_LOCATION);
		reference.flags.add(FLAGS.FLAG_START_POINT);
	}
	
	public void setDepth(double depth) {
		setZ(depth, ZUnits.DEPTH);
	}
	
	public void setAltitude(double alt) {
		setZ(alt, ZUnits.ALTITUDE);
	}
	
	public void setZ(double value, ZUnits units) {
		DesiredZ z = new DesiredZ();
		z.value = (float) value;
		z.z_units = units;
		reference.z = z;
		reference.flags.add(FLAGS.FLAG_Z);
	}
	
	public void setLoiterRadius(double radius) {
		reference.radius = (float)radius;
		reference.flags.add(FLAGS.FLAG_RADIUS);
	}
	
	public void end() {
		reference.flags.add(FLAGS.FLAG_MANDONE);
		finished = true;
	}
	
	public void setSpeed(double value, SpeedUnits units) {
		DesiredSpeed speed = new DesiredSpeed();
		speed.value = (float) value;
		speed.speed_units = units;
		reference.speed = speed;
		reference.flags.add(FLAGS.FLAG_SPEED);
	}
	
	
	public boolean arrivedXY() {
		FollowRefState refState = get(FollowRefState.class);
		if (refState == null || refState.reference == null)
			return false;
		
		if (refState.reference.lat != reference.lat || refState.reference.lon != reference.lon)
			return false;
		
		return refState.proximity.contains(FollowRefState.PROXIMITY.PROX_XY_NEAR);
	}
	
	public boolean arrivedZ() {
		FollowRefState refState = get(FollowRefState.class);
		if (refState == null || refState.reference == null || refState.reference.z == null)
			return false;
		if (refState.reference.z.z_units != reference.z.z_units || refState.reference.z.value != reference.z.value)
			return false;
		return refState.proximity.contains(FollowRefState.PROXIMITY.PROX_Z_NEAR);
	}	
	
	
	
	
	@Consume
	protected void on(VehicleState msg) {
		if (msg.src == remoteSrc) {
			if (msg.op_mode == OP_MODE.VS_SERVICE && shouldStart()) {
				startExecution();
			}
		}
	}

	@Consume
	protected void on(Message msg) {
		if (msg.src == remoteSrc) {
			synchronized (state) {
				state.put(msg.mgid(), msg);
			}
		}
	}

	@Consume
	protected void on(PlanControlState msg) {
		if (msg.src == remoteSrc) {
			if (msg.state == STATE.PCS_EXECUTING && msg.plan_id.equals(PLAN_NAME))
				executing = true;
			else
				executing = false;
		}
	}

	@Consume
	protected void on(Abort msg) {
		if (msg.src == remoteSrc || msg.dst == remoteSrc) {
			System.err.println("ABORTED.");
			finished = true;
		}
	}

	private boolean shouldStart() {
		return !finished && !executing && (System.currentTimeMillis() - startCommandTime) > 3000;
	}

	private void startExecution() {
		PlanControl pc = new PlanControl();
		pc.plan_id = PLAN_NAME;
		pc.op = OP.PC_START;
		pc.type = TYPE.PC_REQUEST;
		pc.request_id = 678;
		
		FollowReference man = new FollowReference();
		man.control_src = localSrc;
		man.control_ent = 255;
		man.loiter_radius = 10;
		man.timeout = 15;

		PlanManeuver pm = new PlanManeuver();
		pm.maneuver_id = "1";
		pm.data = man;

		PlanSpecification ps = new PlanSpecification();
		ps.plan_id = PLAN_NAME;
		ps.start_man_id = "1";
		ps.maneuvers.add(pm);
		pc.arg = ps;

		try {
			send(pc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		startCommandTime = System.currentTimeMillis();
	}

	@Periodic(1000)
	public final void sendRef() {

		if (finished)
			System.exit(0);
		
		if (executing) {
			FollowRefState curState = get(FollowRefState.class);
			if (curState == null)
				return;

			update(curState);
			
			try {
				send(reference);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends Message> T get(Class<T> clazz) {
		int id = MessageFactory.idOf(clazz.getSimpleName());
		synchronized (state) {
			return (T) state.get(id);
		}
	}

	public abstract void update(FollowRefState fref);
	
	public BackSeatDriver() {
		super();
		register(this);
	}

}
