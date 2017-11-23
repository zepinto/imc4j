package pt.lsts.backseat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
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
import pt.lsts.imc4j.msg.TransmissionRequest.COMM_MEAN;
import pt.lsts.imc4j.msg.TransmissionRequest.DATA_MODE;
import pt.lsts.imc4j.msg.TransmissionStatus;
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
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	private ConcurrentHashMap<COMM_MEAN, LinkedBlockingDeque<TransmissionRequest>> pendingRequests = new ConcurrentHashMap<>();
	private ConcurrentHashMap<COMM_MEAN, TransmissionRequest> ongoingRequests = new ConcurrentHashMap<>();
	
	{
		for (COMM_MEAN mean : COMM_MEAN.values()) {
			pendingRequests.put(mean, new LinkedBlockingDeque<>());
			//ongoingRequests.put(mean, null);
		}		
	}
	
	public void setLocation(double latDegs, double lonDegs) {
		reference.lat = Math.toRadians(latDegs);
		reference.lon = Math.toRadians(lonDegs);
		reference.flags.add(FLAGS.FLAG_LOCATION);
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
			System.err.println("ABORTED.");
			finished = true;
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

	private LinkedHashMap<Integer, Message> requests_sent = new LinkedHashMap<>();
	private AtomicInteger request_id = new AtomicInteger(10000);

	@Consume
	public void on(TransmissionStatus status) {
		if (status.src != remoteSrc)
			return;

		synchronized (requests_sent) {
			if (requests_sent.containsKey(status.req_id)) {
				requests_sent.put(status.req_id, status);
			}
		}
		
		TransmissionRequest ongoing = ongoingRequests.searchValues(1, t -> t.req_id == status.req_id? t : null);
		if (ongoing != null && status.status != TransmissionStatus.STATUS.TSTAT_IN_PROGRESS) {
			print("Request "+ongoing.req_id+" has finished: "+status.status);
			ongoingRequests.remove(ongoing.comm_mean);
			sendPending();
		}
	}

	protected TransmissionRequest createRequest(Message msg, TransmissionRequest.COMM_MEAN mean, int ttl) {
		TransmissionRequest request = new TransmissionRequest();
		request.data_mode = DATA_MODE.DMODE_INLINEMSG;
		request.msg_data = msg;
		request.comm_mean = mean;
		request.destination = "broadcast";
		request.deadline = System.currentTimeMillis() / 1000.0 + ttl;
		request.req_id = request_id.incrementAndGet();
		return request;
	}

	protected TransmissionRequest createRequest(String text, TransmissionRequest.COMM_MEAN mean, int ttl) {
		TransmissionRequest request = new TransmissionRequest();
		request.data_mode = DATA_MODE.DMODE_TEXT;
		request.txt_data = text;
		request.comm_mean = mean;
		request.destination = "broadcast";
		request.deadline = System.currentTimeMillis() / 1000.0 + ttl;
		request.req_id = request_id.incrementAndGet();
		return request;
	}

	protected void waitFor(TransmissionRequest request) throws Exception {
		long deadline = (long) (request.deadline * 1000.0);
		while (System.currentTimeMillis() < deadline) {
			Thread.sleep(100);
			synchronized (requests_sent) {
				if (requests_sent.get(request.req_id) instanceof TransmissionStatus) {
					TransmissionStatus status = (TransmissionStatus) requests_sent.get(request.req_id);
					switch (status.status) {
					case TSTAT_DELIVERED:
					case TSTAT_SENT:
					case TSTAT_MAYBE_DELIVERED:
						requests_sent.remove(request.req_id);			
						return;
					case TSTAT_INPUT_FAILURE:
					case TSTAT_TEMPORARY_FAILURE:
					case TSTAT_PERMANENT_FAILURE:
						requests_sent.remove(request.req_id);
						throw new Exception(status.info);
					default:
						break;
					}
				}
			}
		}
		requests_sent.remove(request.req_id);
		throw new Exception("Transmission timed out.");
	}
	
	void sendPending() {
		for (TransmissionRequest.COMM_MEAN mean : pendingRequests.keySet()) {
			// is there a transmission that can be sent now?
			if (!ongoingRequests.containsKey(mean)) {
				TransmissionRequest req;
				do {
					req = pendingRequests.get(mean).pollFirst();
				}
				while (req != null && req.deadline < System.currentTimeMillis()/1000.0);
				
				if (req != null) {
					ongoingRequests.put(mean, req);
					
					try {
						send(req);
						print("Sending "+req.req_id+" request using "+mean);
						requests_sent.put(req.req_id, req);
					}							
					catch (Exception e) {
						e.printStackTrace();
					}	
				}				
			}
		}
	}

	protected int messagesPending() {
		return ongoingRequests.size() + pendingRequests.values().stream().mapToInt(t -> t.size()).sum();
	}
	
	protected Future<Void> sendVia(Message msg, TransmissionRequest.COMM_MEAN mean, int ttl) {
		final TransmissionRequest request = createRequest(msg, mean, ttl);
		pendingRequests.get(mean).add(request);
		
		sendPending();
		
		return executor.submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				waitFor(request);
				return null;
			}
		});
		
	}

	protected Future<Void> sendVia(String text, TransmissionRequest.COMM_MEAN mean, int ttl) {
		final TransmissionRequest request = createRequest(text, mean, ttl);
		pendingRequests.get(mean).add(request);

		sendPending();
				
		return executor.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				waitFor(request);
				return null;
			}
		});
	}
	
	protected Future<Void> sendViaIridium(Message msg, int ttl) {
		return sendVia(msg, TransmissionRequest.COMM_MEAN.CMEAN_SATELLITE, ttl);
	}

	protected Future<Void> sendViaIridium(String text, int ttl) {
		return sendVia(text, TransmissionRequest.COMM_MEAN.CMEAN_SATELLITE, ttl);
	}

	protected Future<Void> sendViaSms(String text, int ttl) {
		return sendVia(text, TransmissionRequest.COMM_MEAN.CMEAN_GSM, ttl);
	}
}
