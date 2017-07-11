package pt.lsts.autonomy;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.LinkedHashMap;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.AlignmentState;
import pt.lsts.imc4j.msg.EntityParameter;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.GpsFix;
import pt.lsts.imc4j.msg.GpsFix.VALIDITY;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;
import pt.lsts.imc4j.msg.PlanControl;
import pt.lsts.imc4j.msg.PlanControl.OP;
import pt.lsts.imc4j.msg.PlanControl.TYPE;
import pt.lsts.imc4j.msg.PlanControlState;
import pt.lsts.imc4j.msg.PlanControlState.STATE;
import pt.lsts.imc4j.msg.PlanManeuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.PlanTransition;
import pt.lsts.imc4j.msg.SetEntityParameters;
import pt.lsts.imc4j.msg.TextMessage;
import pt.lsts.imc4j.msg.VehicleMedium;
import pt.lsts.imc4j.net.TcpClient;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.WGS84Utilities;

public class MissionExecutive extends TcpClient {

	private LinkedHashMap<Integer, Message> msgs = new LinkedHashMap<>();
	protected PlanControl planCommand = null;
	protected int count = 1;
	protected boolean useBroadcast = true;

	protected State state = null;

	public MissionExecutive() {
		super();
		register(this);
	}

	@Consume
	protected void on(Message msg) {
		if (msg.src == remoteSrc) {
			synchronized (msgs) {
				msgs.put(msg.mgid(), msg);
			}
		}
	}

	@Consume
	protected void on(Abort abort) {
		System.err.println("Received ABORT. Terminating.");
		System.exit(1);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Message> T get(Class<T> clazz) {
		int id = MessageFactory.idOf(clazz.getSimpleName());
		synchronized (msgs) {
			return (T) msgs.get(id);
		}
	}

	protected boolean ready() {
		PlanControlState pcs = get(PlanControlState.class);
		return pcs != null && pcs.state == STATE.PCS_READY;
	}

	protected boolean atSurface() {
		VehicleMedium medium = get(VehicleMedium.class);
		return medium != null && medium.medium.equals(VehicleMedium.MEDIUM.VM_WATER);
	}

	protected boolean hasGps() {
		GpsFix fix = get(GpsFix.class);
		return fix != null && fix.validity.contains(VALIDITY.GFV_VALID_POS);
	}

	protected boolean imuIsAligned() {
		AlignmentState state = get(AlignmentState.class);
		return state != null && state.state == pt.lsts.imc4j.msg.AlignmentState.STATE.AS_ALIGNED;
	}
	
	protected void setParam(String entity, String param, String value) {
		SetEntityParameters params = new SetEntityParameters();
		params.name = entity;
		EntityParameter p = new EntityParameter();
		p.name = param;
		p.value = value;
		params.params.add(p);
		
		try {
			send(params);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void activate(String entity) {
		setParam(entity, "Active", "true");
	}
	
	protected void deactivate(String entity) {
		setParam(entity, "Active", "false");
	}

	protected void broadcast(Message msg) throws IOException {
		DatagramSocket socket = new DatagramSocket();
		msg.src = localSrc;
		msg.timestamp = System.currentTimeMillis() / 1000.0;
		InetAddress multicast = InetAddress.getByName("224.0.75.69");
		InetAddress broadcast = InetAddress.getByName("255.255.255.255");
		byte[] data = msg.serialize();

		for (int port = 30100; port < 30105; port++) {
			socket.send(new DatagramPacket(data, data.length, multicast, port));
			if (useBroadcast)
				socket.send(new DatagramPacket(data, data.length, broadcast, port));
		}
		socket.close();
	}

	public double[] position() {
		return WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
	}

	public PlanSpecification spec(Maneuver... maneuvers) {
		PlanSpecification spec = new PlanSpecification();
		spec.plan_id = "MExec_" + (count++);

		for (int i = 0; i < maneuvers.length; i++) {
			Maneuver m = maneuvers[i];
			PlanManeuver pm = new PlanManeuver();
			pm.data = m;
			pm.maneuver_id = "" + (i + 1);
			spec.maneuvers.add(pm);

			if (i > 0) {
				PlanTransition trans = new PlanTransition();
				trans.conditions = "ManeuverIsDone";
				trans.source_man = "" + i;
				trans.dest_man = "" + (i + 1);
				spec.transitions.add(trans);
			}
		}
		spec.start_man_id = "1";

		return spec;
	}

	public void exec(Maneuver... maneuvers) {
		if (maneuvers.length == 0)
			stopPlan();
		else
			exec(spec(maneuvers));
	}

	void exec(PlanSpecification plan) {
		PlanControl pc = new PlanControl();
		pc.plan_id = plan.plan_id;
		pc.arg = plan;
		pc.op = OP.PC_START;
		pc.type = TYPE.PC_REQUEST;
		pc.dst = remoteSrc;
		try {
			send(pc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void startPlan(String id) {
		PlanControl pc = new PlanControl();
		pc.plan_id = id;
		pc.arg = null;
		pc.op = OP.PC_START;
		pc.type = TYPE.PC_REQUEST;
		pc.dst = remoteSrc;
		try {
			send(pc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void stopPlan() {
		PlanControl pc = new PlanControl();
		pc.op = OP.PC_STOP;
		pc.type = TYPE.PC_REQUEST;
		pc.dst = remoteSrc;
		try {
			send(pc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	Thread multicastThread = null;

	@Override
	public void run() {
		multicastThread = new Thread("multicast") {
			@Override
			public void run() {
				MulticastSocket msock = null;
				byte[] buffer = new byte[65535];
				try {
					for (int port = 30100; port < 30105; port++) {
						try {
							msock = new MulticastSocket(port);
							msock.joinGroup(InetAddress.getByName("224.0.75.69"));
							System.out.println("Listening on port " + port);
							break;
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				while (connected) {
					try {
						DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
						msock.receive(packet);
						Message m = SerializationUtils.deserializeMessage(buffer);
						if (m != null)
							dispatch(m);
					} catch (Exception e) {
						e.printStackTrace();
						break;
					}
				}

				try {
					msock.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		multicastThread.start();
		super.run();
	}

	@Override
	public void interrupt() {
		super.interrupt();
		if (multicastThread != null)
			multicastThread.interrupt();
	}

	public static void main(String[] args) throws Exception {
		MissionExecutive executive = PojoConfig.create(MissionExecutive.class, args);
		executive.connect("127.0.0.1", 6003);
		while (true) {
			Thread.sleep(10000);
			TextMessage m = new TextMessage();
			m.text = "whaaat?";
			executive.broadcast(m);
		}
	}

	@Periodic(1000)
	public void update() {
		if (state == null)
			System.exit(0);
		else
			state = state.step();
	}

	@FunctionalInterface
	public static interface State {
		public State step();
	}
}
