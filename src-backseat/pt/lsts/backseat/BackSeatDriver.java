package pt.lsts.backseat;

import java.util.EnumSet;
import java.util.LinkedHashMap;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.backseat.TcpClient;
import pt.lsts.imc4j.msg.Abort;
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
import pt.lsts.imc4j.msg.VehicleState;
import pt.lsts.imc4j.msg.VehicleState.OP_MODE;

public abstract class BackSeatDriver extends TcpClient {

	protected LinkedHashMap<Integer, Message> state = new LinkedHashMap<>();
	protected long startCommandTime = 0;
	protected boolean executing = false, finished = false;
	protected static final String PLAN_NAME = "back_seat";

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

			Reference ref = drive(curState);
			if (ref == null) {
				finished = true;
				ref = new Reference();
				ref.flags = EnumSet.of(Reference.FLAGS.FLAG_MANDONE);
			}
			try {
				send(ref);
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

	public abstract Reference drive(FollowRefState fref);
	
	public BackSeatDriver() {
		super();
		register(this);
	}

}
