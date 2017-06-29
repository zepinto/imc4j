package pt.lsts.mvplanner;

import java.util.Date;

import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.ScheduledGoto;
import pt.lsts.imc4j.msg.ScheduledGoto.DELAYED;
import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalAction.TYPE;
import pt.lsts.imc4j.util.PlanUtilities;

public class MoveAction extends AbstractPddlAction {
    
	public MoveAction(PddlLocation location, Date startTime, Date endTime) {
		setStartTime(startTime);
		setEndTime(endTime);
		setAssociatedLocation(location);
	}
    
    @Override
    public PlanSpecification getBehavior() {
        ScheduledGoto man = new ScheduledGoto();
        man.arrival_time = getEndTime().getTime()/1000.0;
        man.delayed = DELAYED.DBEH_RESUME;
        man.lat = Math.toRadians(getAssociatedLocation().latDegs);
        man.lon = Math.toRadians(getAssociatedLocation().lonDegs);
        man.travel_z = CommonSettings.Z;
        man.travel_z_units = ZUnits.DEPTH;
        return PlanUtilities.createPlan("mvplan_"+getAssociatedTask(), man);        
    }
    
    @Override
    public TemporalAction asImc() {
    	TemporalAction action = super.asImc();
    	action.type = TYPE.ATYPE_MOVE;    	
    	return action;
    }
}
