package pt.lsts.backseat;

import pt.lsts.imc4j.msg.FollowRefState;

public class FSMController extends BackSeatDriver {
	
	protected FSMState state = null;
	
	@Override
	public void update(FollowRefState fref) {
		if (state == null) {
			end();
		}
		else {
			try {
                state = state.step(fref);
            }
            catch (Exception e) {
                e.printStackTrace();
                end();
            }
		}
	}
	
    protected void printFSMStateName(String name) {
        print("FSM> Steping into state " + name);
    }

	@FunctionalInterface
	public static interface FSMState {
		public FSMState step(FollowRefState refState);
	}
}
