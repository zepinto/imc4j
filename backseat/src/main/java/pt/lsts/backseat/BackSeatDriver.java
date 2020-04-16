package pt.lsts.backseat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.DesiredSpeed;
import pt.lsts.imc4j.msg.DesiredZ;
import pt.lsts.imc4j.msg.EntityParameter;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.FollowReference;
import pt.lsts.imc4j.msg.GpsFix;
import pt.lsts.imc4j.msg.IridiumTxStatus;
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
import pt.lsts.imc4j.msg.SetEntityParameters;
import pt.lsts.imc4j.msg.TransmissionRequest;
import pt.lsts.imc4j.msg.TransmissionRequest.DATA_MODE;
import pt.lsts.imc4j.msg.TransmissionStatus;
import pt.lsts.imc4j.msg.TransmissionStatus.STATUS;
import pt.lsts.imc4j.msg.VehicleMedium;
import pt.lsts.imc4j.msg.VehicleMedium.MEDIUM;
import pt.lsts.imc4j.net.TcpClient;
import pt.lsts.imc4j.util.WGS84Utilities;

public abstract class BackSeatDriver extends TcpClient {

	protected LinkedHashMap<Integer, Message> state = new LinkedHashMap<>();
	protected long startCommandTime = 0;
	protected boolean paused = true, finished = false;
	protected String endPlan = null, plan_name = "back_seat";
	private Reference reference = new Reference();
	private final double MAX_NEAR_DIST = 50;
	
	private static boolean IRIDIUM_SIMULATION = false;
	
	private LinkedHashMap<Integer, TransmissionRequest> iridiumTransmissions = new LinkedHashMap<>();
	
	public void setLocation(double latDegs, double lonDegs) {
		reference.lat = Math.toRadians(latDegs);
		reference.lon = Math.toRadians(lonDegs);
		reference.flags.add(FLAGS.FLAG_LOCATION);
	}
	
	public double[] getDestinationDegs() {
		return new double[] {Math.toDegrees(reference.lat), Math.toDegrees(reference.lon)};
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
		reference.radius = (float) radius;
		if (radius != 0)
			reference.flags.add(FLAGS.FLAG_RADIUS);
		else
			reference.flags.remove(FLAGS.FLAG_RADIUS);

	}

	/**
	 * @return the plan_name
	 */
	public final String getPlanName() {
		return plan_name;
	}

	/**
	 * @param plan_name
	 *            the plan_name to set
	 */
	public final void setPlanName(String plan_name) {
		this.plan_name = plan_name;
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
		EstimatedState state = get(EstimatedState.class);
		
		if (refState == null || refState.reference == null || state == null)
			return false;
		
		// check if vehicle is actually near the destination
		double lld[] = WGS84Utilities.toLatLonDepth(state);
		
		double dist = WGS84Utilities.distance(lld[0], lld[1], Math.toDegrees(reference.lat), Math.toDegrees(reference.lon));
		if (dist > MAX_NEAR_DIST)
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

	public boolean isUnderwater() {
		try {
			return get(VehicleMedium.class).medium == MEDIUM.VM_UNDERWATER;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean hasGps() {
		try {
			return get(GpsFix.class).validity.contains(GpsFix.VALIDITY.GFV_VALID_POS);
		} catch (Exception e) {
			return false;
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

	protected boolean isControlling() {
		PlanControlState msg = get(PlanControlState.class);
		return (msg != null && msg.state == STATE.PCS_EXECUTING && msg.plan_id.equals(plan_name));
	}

	protected boolean isIdle() {
		PlanControlState msg = get(PlanControlState.class);
		return (msg != null && msg.state == STATE.PCS_READY);
	}

	@Consume
	protected void on(Abort msg) {
		if (msg.src == remoteSrc || msg.dst == remoteSrc) {
			print("ABORTED.");
			paused = true;
		}
	}

	private boolean shouldStart() {
		return !finished && !paused && isIdle() && (System.currentTimeMillis() - startCommandTime) > 3000
				&& get(EstimatedState.class) != null;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public void stopExecution() {
		PlanControl pc = new PlanControl();
		pc.plan_id = plan_name;
		pc.op = OP.PC_STOP;
		pc.type = TYPE.PC_REQUEST;
		pc.request_id = 679;

		try {
			send(pc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void startPlan(String id) {
		finished = true;
		PlanControl pc = new PlanControl();
		pc.plan_id = id;
		pc.op = OP.PC_START;
		pc.type = TYPE.PC_REQUEST;
		pc.request_id = 679;

		try {
			send(pc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	private void startExecution() {

		double[] pos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		setLocation(pos[0], pos[1]);

		PlanControl pc = new PlanControl();
		pc.plan_id = plan_name;
		pc.op = OP.PC_START;
		pc.type = TYPE.PC_REQUEST;
		pc.request_id = 678;

		FollowReference man = new FollowReference();
		man.control_src = localSrc;
		man.control_ent = 255;
		man.loiter_radius = 10;
		man.timeout = 15;
		man.altitude_interval = 0.5f;

		PlanManeuver pm = new PlanManeuver();
		pm.maneuver_id = "1";
		pm.data = man;

		PlanSpecification ps = new PlanSpecification();
		ps.plan_id = plan_name;
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
	
	@Periodic(45_000)
	public void iridiumSimulation() {
		if (!IRIDIUM_SIMULATION)
			return;

		VehicleMedium medium = get(VehicleMedium.class);
		if (medium == null || medium.medium == MEDIUM.VM_UNDERWATER)
			return;
	
		synchronized (iridiumTransmissions) {
			for (Entry<Integer, TransmissionRequest> entry : iridiumTransmissions.entrySet()) {
				TransmissionStatus status = new TransmissionStatus();
				status.req_id = entry.getKey();
				status.status = STATUS.TSTAT_DELIVERED;
				System.out.println("Simulated delivery of "+entry.getKey());
				dispatch(status);
				
			}
		}
		
		IridiumTxStatus empty = new IridiumTxStatus();
		empty.src = remoteSrc;		
		empty.status = IridiumTxStatus.STATUS.TXSTATUS_EMPTY;
		dispatch(empty);
	}
	
	@Periodic(5000)
	public final void printState() {
		EstimatedState state = get(EstimatedState.class);
		VehicleMedium medium = get(VehicleMedium.class);
		if (state == null || medium == null)
			return;
		
		double[] pos = WGS84Utilities.toLatLonDepth(state);
		
		print("POS: "+pos[0]+" / "+pos[1]+", "+state.depth+" : "+state.alt+" : "+medium.medium);
	}

	@Periodic(1000)
	public final void controlLoop() {

		if (finished) {
			if (endPlan != null) {
				print("Starting execution of '" + endPlan + "'.");
				startPlan(endPlan);

				try {
					print("Waiting 5s to tidy everything up before exiting...");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			disconnect();
		} else if (shouldStart()) {
			startExecution();
		} else if (isControlling()) {
			if (paused) {
				stopExecution();
			} else {
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
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T get(Class<T> clazz) {
		int id = MessageFactory.idOf(clazz.getSimpleName());
		synchronized (state) {
			return (T) state.get(id);
		}
	}

	public abstract void update(FollowRefState fref);

	public BackSeatDriver() {
		super();
		register(this);
		
		if (IRIDIUM_SIMULATION) {
			System.err.println("Simulated delivery of messages!");
		}
	}

	protected void setParam(String entity, String param, String value) {
		setParam(entity, new String[] { param, value });
	}

	protected void setParam(String entity, String... paramValue) {
		if (paramValue == null || paramValue.length < 2) {
			print("No parameters to set for entity " + entity);
			return;
		}

		SetEntityParameters params = new SetEntityParameters();
		params.name = entity;

		for (int i = 1; i < paramValue.length; i = i + 2) {
			try {
				String param = paramValue[i - 1];
				String value = paramValue[i];
				EntityParameter p = new EntityParameter();
				p.name = param;
				p.value = value;
				params.params.add(p);
			} catch (Exception e) {
				print(String.format("Error '%s' setting parameters message to send for %s with %s", e, entity,
						paramValue == null || paramValue.length == 0 ? "EMPTY" : Arrays.toString(paramValue)));
			}
		}

		try {
			send(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void activate(String entity) {
		setParam(entity, "Active", "true");
	}

	protected void activate(String entity, String... paramValue) {
		if (paramValue == null || paramValue.length < 2) {
			setParam(entity, "Active", "true");
		} else {
			String[] params = new String[2 + paramValue.length];
			params[0] = "Active";
			params[1] = "true";
			for (int i = 0; i < paramValue.length; i++) {
				params[i + 2] = paramValue[i];
			}
			setParam(entity, paramValue);
		}
	}

	protected void deactivate(String entity) {
		setParam(entity, "Active", "false");
	}

	private AtomicInteger request_id = new AtomicInteger(10000);

	@Consume
	public void on(TransmissionStatus status) {
		print("Received transmission status: " + status);

		if (!iridiumTransmissions.containsKey(status.req_id)) {
			print("Unrecognized transmission status: " + status);
			return;
		}
		
		print("Iridium transmission status changed: " + status);
		switch (status.status) {
		case TSTAT_DELIVERED:
		case TSTAT_MAYBE_DELIVERED:
		case TSTAT_RANGE_RECEIVED:
			print("Request " + status.req_id + " has been transmitted: " + status.status+" / "+status.info);
			iridiumTransmissions.remove(status.req_id);
			break;
		case TSTAT_INPUT_FAILURE:
		case TSTAT_TEMPORARY_FAILURE:
		case TSTAT_PERMANENT_FAILURE:
			print("Request " + status.req_id + " could not be transmitted: " + status.status+" / "+status.info);
			iridiumTransmissions.remove(status.req_id);
			break;
		default:
			break;
		}
	}

	protected TransmissionRequest inlineMsgRequest(Message msg, TransmissionRequest.COMM_MEAN mean, int ttl) {
		TransmissionRequest request = new TransmissionRequest();
		request.data_mode = DATA_MODE.DMODE_INLINEMSG;
		request.msg_data = msg;
		request.comm_mean = mean;
		request.destination = "broadcast";
		request.deadline = System.currentTimeMillis() / 1000.0 + ttl;
		request.req_id = request_id.incrementAndGet();
		return request;
	}

	protected TransmissionRequest txtMessageRequest(String text, TransmissionRequest.COMM_MEAN mean, int ttl) {
		TransmissionRequest request = new TransmissionRequest();
		request.data_mode = DATA_MODE.DMODE_TEXT;
		request.txt_data = text;
		request.comm_mean = mean;
		request.destination = "broadcast";
		request.deadline = System.currentTimeMillis() / 1000.0 + ttl;
		request.req_id = request_id.incrementAndGet();
		return request;
	}

	protected void sendViaIridium(Message msg, int ttl) {
		TransmissionRequest request = inlineMsgRequest(msg, TransmissionRequest.COMM_MEAN.CMEAN_SATELLITE, ttl);
		try {
			send(request);
			iridiumTransmissions.put(request.req_id, request);
			print("Request to send "+msg.abbrev()+" over Iridium: "+request.req_id);
		}
		catch (Exception e) {
			print("Could not transmit Iridium message: "+e.getMessage());
		}
	}

	protected void sendViaIridium(String text, int ttl) {
		TransmissionRequest request = txtMessageRequest(text, TransmissionRequest.COMM_MEAN.CMEAN_SATELLITE, ttl);
		try {
			send(request);
			iridiumTransmissions.put(request.req_id, request);
			print("Request to send text '"+text+"' over Iridium: "+request.req_id);
		}
		catch (Exception e) {
			print("Error while trying to send Iridium request: "+e.getMessage());
		}
	}

	protected void sendViaSms(String text, int ttl) {
		TransmissionRequest request = txtMessageRequest(text, TransmissionRequest.COMM_MEAN.CMEAN_GSM, ttl);
		try {
			send(request);
			print("Request to send text '"+text+"' over SMS: "+request.req_id);
		}
		catch (Exception e) {
			print("Error while trying to send GSM request: "+e.getMessage());
		}
	}
}
