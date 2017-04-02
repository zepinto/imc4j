package pt.lsts.backseat;

import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.Reference;

public class FSMController extends BackSeatDriver {
	
	protected FSMState state = null;
	protected Reference reference = new Reference();
	
	@Override
	public Reference drive(FollowRefState fref) {
		if (state == null)
			return null;
		else
			state = state.step(fref);
		return reference;
	}
	
	@FunctionalInterface
	public static interface FSMState {
		public FSMState step(FollowRefState refState);
	}
}
