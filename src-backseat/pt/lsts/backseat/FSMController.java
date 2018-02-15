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
	
    protected void printFSMState() {
    	String method = currentThread().getStackTrace()[2].getMethodName();
        print("FSM State: " + method);
    }

	@FunctionalInterface
	public static interface FSMState {
		public FSMState step(FollowRefState refState);
	}
}
