package pt.lsts.autonomy.soi;

import java.time.LocalDateTime;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.endurance.Plan;
import pt.lsts.endurance.Waypoint;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.Salinity;
import pt.lsts.imc4j.msg.SoiCommand;
import pt.lsts.imc4j.msg.SoiCommand.COMMAND;
import pt.lsts.imc4j.msg.SoiCommand.TYPE;
import pt.lsts.imc4j.util.WGS84Utilities;

public class PlumeTracker extends TimedFSM {

	@Parameter(description = "Start angle, in degrees")
	double start_ang = -180;

	@Parameter(description = "Variation, in degrees, between survey angles")
	double angle_inc = 10;

	@Parameter(description = "Salinity threshold, below which the water is considered to be fresh.")
	double sal_threshold = 33;

	private double angle;
	
	public PlumeTracker() {
		state = this::init;
	}
	
	FSMState init(FollowRefState state) {
		this.angle = start_ang;
		double[] target = go_out(angle);
		setTarget(target[0], target[1]);
		return this::find_plume;
	}
	
	FSMState find_plume(FollowRefState state) {
		double salinity = get(Salinity.class).value;
		
		if (salinity > sal_threshold)
			return this::track_plume;
		else
			return this::find_plume;
	}
	
	FSMState track_plume(FollowRefState state) {
		return this::track_plume;
	}
		
	
	double[] go_out(double angle) {
		double angRads = Math.toRadians(angle);
		double offsetX = Math.cos(angRads) * 1000;
		double offsetY = Math.sin(angRads) * 1000;
		double[] target = WGS84Utilities.WGS84displace(0, 0, 0, offsetX, offsetY, 0);
		
		return target;
	}
	
	private Plan setTarget( double destLat, double destLon) {
		Plan plan = new Plan("go");
		
		double lld[] = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
		Waypoint wpt1 = new Waypoint(0, (float)lld[0], (float)lld[1]);
		Waypoint wpt2 = new Waypoint(1, (float)destLat, (float)destLon);
		
		plan.addWaypoint(wpt1);
		plan.addWaypoint(wpt2);
		
		try {
			SoiCommand newPlan = new SoiCommand();
			newPlan.src = remoteSrc;
			newPlan.command = COMMAND.SOICMD_EXEC;
			newPlan.type = TYPE.SOITYPE_REQUEST;
			newPlan.plan = plan.asImc();
			newPlan.info = "DRiP Plan generated on "+LocalDateTime.now();
			on(newPlan);
			return plan;
		}
		catch (Exception e) {
			printError("Could not generate plan.");
			return null;
		}	
	}
	
}
