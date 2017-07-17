package pt.lsts.mvplanner;

import java.util.Date;

import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalAction.STATUS;

public abstract class AbstractPddlAction implements IPddlAction {

    protected String task = null;
    protected int vehicle = -1;
    protected Date start = null, end = null;
    protected PddlLocation associatedLocation = null;
    protected PlanSpecification associatedPlan = null;
    protected static int counter = 1;
    protected String id = "";
    
    
    @Override
    public String getAssociatedTask() {
        return task;
    }

    @Override
    public int getAssociatedVehicle() {
        return vehicle;
    }

    @Override
    public Date getStartTime() {
        return start;
    }
    
    public void setStartTime(Date start) {
    	this.start = start;
    }

    @Override
    public Date getEndTime() {
        return end;
    }
    
    public void setEndTime(Date end) {
    	this.end = end;
    }


    /**
     * @return the task
     */
    public final String getTask() {
        return task;
    }

    /**
     * @param task the task to set
     */
    public final void setTask(String task) {
        this.task = task;
    }

    /**
     * @return the vehicle
     */
    public final int getVehicle() {
        return vehicle;
    }

    /**
     * @param vehicle the vehicle to set
     */
    public final void setVehicle(int vehicle) {
        this.vehicle = vehicle;
    }
	/**
	 * @return the associatedLocation
	 */
	public final PddlLocation getAssociatedLocation() {
		return associatedLocation;
	}

	/**
	 * @param associatedLocation the associatedLocation to set
	 */
	public final void setAssociatedLocation(PddlLocation associatedLocation) {
		this.associatedLocation = associatedLocation;
	}
	
	@Override
	public PddlLocation getStartLocation() {
		return getAssociatedLocation();
	}
	
	@Override
	public PddlLocation getEndLocation() {
		return getAssociatedLocation();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
    public TemporalAction asImc() {
    	TemporalAction action = new TemporalAction();
    	if (getAssociatedTask() != null)
    		action.action_id = getAssociatedTask();
    	else
    		action.action_id = getId();
    	action.action = getBehavior();
    	action.duration = getDuration();
    	action.start_time = getStartTime().getTime()/1000.0;
    	action.system_id = getVehicle();
    	action.status = STATUS.ASTAT_UKNOWN;
    	return action;
    }

}
