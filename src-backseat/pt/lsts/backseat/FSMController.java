package pt.lsts.backseat;

import pt.lsts.imc4j.msg.FollowRefState;

public class FSMController extends BackSeatDriver {
	
	protected FSMState state = null;
	
	@Override
	public void update(FollowRefState fref) {
		if (state == null)
			end();
		else
			state = state.step(fref);		
	}
	
	@FunctionalInterface
	public static interface FSMState {
		public FSMState step(FollowRefState refState);
	}
}
