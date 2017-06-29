package pt.lsts.mvplanner;

import java.util.Date;

import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalAction.TYPE;

public class SampleAction extends AbstractPddlAction {

	protected PlanSpecification behavior = null;
	
	public SampleAction(PddlLocation location, Date startTime, Date endTime, PlanSpecification spec) {
		setStartTime(startTime);
		setEndTime(endTime);
		setAssociatedLocation(location);
		this.behavior = spec;
	}	
    
    @Override
    public PlanSpecification getBehavior() {
       return behavior;
    }
    
	@Override
    public TemporalAction asImc() {
    	TemporalAction action = super.asImc();
    	action.type = TYPE.ATYPE_SURFACE;
    	return action;
    }
}
