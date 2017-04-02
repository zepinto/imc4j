package pt.lsts.backseat;

import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.DesiredSpeed;
import pt.lsts.imc4j.msg.DesiredZ;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.Reference;
import pt.lsts.imc4j.msg.Reference.FLAGS;
import pt.lsts.imc4j.util.PojoConfig;

public class YoYoFSM extends FSMController {
	
	@Parameter
	double lat = 41.185467;
	
	@Parameter
	double lon = -8.705522;
	
	@Parameter
	float maxDepth = 10;
	
	@Parameter
	float minDepth = 2;
	
	@Parameter
	float speed = 1.2f;
	
	public YoYoFSM() {
		System.out.println("starting");
		init(null);
		state = this::init;
	}
	
	private boolean arrivedXY() {
		FollowRefState refState = get(FollowRefState.class);
		return refState != null && refState.proximity.contains(FollowRefState.PROXIMITY.PROX_XY_NEAR);
	}
	
	private boolean arrivedZ() {
		FollowRefState refState = get(FollowRefState.class);
		EstimatedState eState = get(EstimatedState.class);
		double diff = Math.abs(eState.depth - reference.z.value);
		
		return diff <= (maxDepth-minDepth)/2  && refState != null && refState.proximity.contains(FollowRefState.PROXIMITY.PROX_Z_NEAR);
	}
	
	public FSMState init(FollowRefState ref) {
		reference = new Reference();
		reference.lat = Math.toRadians(lat);
		reference.lon = Math.toRadians(lon);
		
		reference.speed = new DesiredSpeed();
		reference.speed.value = speed;
		reference.speed.speed_units = SpeedUnits.METERS_PS;
		
		reference.z = new DesiredZ();
		reference.z.value = 0;
		reference.z.z_units = ZUnits.DEPTH;
		
		reference.flags.add(FLAGS.FLAG_LOCATION);
		reference.radius = 0;
		
		return this::descending;		
	}
	
	public FSMState descending(FollowRefState ref) {
		reference.z.value = maxDepth;
		
		if(arrivedXY())
			return this::surfacing;
		
		if (arrivedZ())
			return this::ascending;	
		else
			return this::descending;
	}
	
	public FSMState ascending(FollowRefState ref) {
		reference.z.value = minDepth;
		
		if(arrivedXY())
			return this::surfacing;
		
		if (arrivedZ())
			return this::descending;
		else
			return this::ascending;
	}
	
	public FSMState surfacing(FollowRefState ref) {
		reference.z.value = 0;
		if (arrivedZ()) {
			counter = 0;
			return this::waiting;
		}
		else
			return this::surfacing;
	}
	
	int counter = 0;
	public FSMState waiting(FollowRefState ref) {
		if (counter++ > 60)
			return this::init;
		else
			return this::waiting;
	}
	
	public static void main(String[] args) throws Exception {
		YoYoFSM yoyo = PojoConfig.create(YoYoFSM.class, args);
		yoyo.connect("127.0.0.1", 6003);
		yoyo.join();
	}

}
