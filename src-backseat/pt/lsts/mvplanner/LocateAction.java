package pt.lsts.mvplanner;
import java.util.Date;

import pt.lsts.imc4j.msg.TemporalAction;
import pt.lsts.imc4j.msg.TemporalAction.TYPE;

public class LocateAction extends SurfaceAction {
    
	public LocateAction(PddlLocation location, Date startTime, Date endTime) {
		super(location, startTime, endTime);
	}	
	
	@Override
    public TemporalAction asImc() {
    	TemporalAction action = super.asImc();
    	action.type = TYPE.ATYPE_LOCATE;
    	return action;
    }
}
