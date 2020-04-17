package pt.lsts.backseat;

import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.util.PojoConfig;

public class YoYoFSM extends FSMController {
	
	@Parameter
	double lat = 41.185467;
	
	@Parameter
	double lon = -8.705522;
	
	@Parameter
	float maxDepth = 5;
	
	@Parameter
	float minDepth = 1;
	
	@Parameter
	float speed = 1.2f;
	
	public YoYoFSM() {
		setLocation(lat, lon);
		setSpeed(speed, SpeedUnits.METERS_PS);
		setDepth(0);
		setLoiterRadius(0);
		state = this::descending;
	}
	
	public FSMState descending(FollowRefState ref) {
		setDepth(maxDepth);
		
		if(arrivedXY())
			return this::surfacing;
		
		if (arrivedZ())
			return this::ascending;	
		else
			return this::descending;
	}
	
	public FSMState ascending(FollowRefState ref) {
		setDepth(minDepth);
		
		if(arrivedXY())
			return this::surfacing;
		
		if (arrivedZ())
			return this::descending;
		else
			return this::ascending;
	}
	
	public FSMState surfacing(FollowRefState ref) {
		setDepth(0);
		return this::surfacing;
	}
	
	public static void main(String[] args) throws Exception {
		YoYoFSM yoyo = PojoConfig.create(YoYoFSM.class, args);
		yoyo.connect("127.0.0.1", 6003);
		yoyo.join();
	}

}
