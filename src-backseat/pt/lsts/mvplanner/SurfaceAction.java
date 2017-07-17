package pt.lsts.mvplanner;

import java.util.Date;

import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.StationKeeping;
import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalAction.TYPE;
import pt.lsts.imc4j.util.PlanUtilities;

public class SurfaceAction extends AbstractPddlAction {
    
	public SurfaceAction(PddlLocation location, Date startTime, Date endTime) {
		setStartTime(startTime);
		setEndTime(endTime);
		setAssociatedLocation(location);
	}	
    
    @Override
    public PlanSpecification getBehavior() {
        StationKeeping man = new StationKeeping();
        man.lat = Math.toRadians(getAssociatedLocation().latDegs);
        man.lon = Math.toRadians(getAssociatedLocation().lonDegs);
        man.speed = CommonSettings.SPEED;
        man.speed_units = CommonSettings.SPEED_UNITS;
        man.z = 0;
        man.z_units = ZUnits.DEPTH;
        man.duration = (int) getDuration();
        man.radius = 10;        
        return PlanUtilities.createPlan(getId(), man);    
    }
    
	@Override
    public TemporalAction asImc() {
    	TemporalAction action = super.asImc();
    	action.type = TYPE.ATYPE_SURFACE;
    	return action;
    }
}
