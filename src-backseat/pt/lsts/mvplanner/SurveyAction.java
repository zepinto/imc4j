package pt.lsts.mvplanner;

import java.util.ArrayList;
import java.util.Date;

import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalAction.TYPE;
import pt.lsts.imc4j.util.PlanUtilities;

public class SurveyAction extends AbstractPddlAction {

	protected PlanSpecification behavior = null;
	private ArrayList<double[]> locs = new ArrayList<>();	
	
	public SurveyAction(PddlLocation location, Date startTime, Date endTime, PlanSpecification spec) {
		setStartTime(startTime);
		setEndTime(endTime);
		setAssociatedLocation(location);
		System.out.println(behavior);
		locs.addAll(PlanUtilities.computeLocations(behavior));
		this.behavior = spec;		
	}	
    
    @Override
    public PlanSpecification getBehavior() {
       return behavior;
    }
    
    @Override
    public PddlLocation getStartLocation() {
		return new PddlLocation(getTask() + "_entry", locs.get(0)[0], locs.get(0)[1]);
    }
    
    @Override
    public PddlLocation getEndLocation() {
    	return new PddlLocation(getTask() + "_exit", locs.get(locs.size()-1)[0], locs.get(locs.size()-1)[1]);
    }
    
    @Override
    public TemporalAction asImc() {
    	TemporalAction action = super.asImc();
    	action.type = TYPE.ATYPE_SURVEY;
    	return action;
    }
}
