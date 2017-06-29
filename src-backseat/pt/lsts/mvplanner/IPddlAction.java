package pt.lsts.mvplanner;

import java.util.Date;

import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.TemporalAction;

public interface IPddlAction extends Comparable<IPddlAction> {

    public PlanSpecification getBehavior();
    public String getAssociatedTask();
    public int getAssociatedVehicle();
    public Date getStartTime();
    public Date getEndTime();
    public PddlLocation getAssociatedLocation();
    public PddlLocation getStartLocation();
    public PddlLocation getEndLocation();
    
    public default double getDuration() {
        return (getEndTime().getTime() - getStartTime().getTime()) / 1000.0;
    }
    
    public default int compareTo(IPddlAction o) {
		return getStartTime().compareTo(o.getStartTime());
    }
    
    public TemporalAction asImc();
}
