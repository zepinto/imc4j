package pt.lsts.backseat;

import java.util.Date;

import pt.lsts.imc4j.msg.FollowRefState;

public class TimedFSM extends FSMController {

	protected Date deadline = new Date(System.currentTimeMillis() + 3600 * 1000);
	
	@Override
	public void update(FollowRefState fref) {
		if (deadline != null && deadline.getTime() < System.currentTimeMillis()) {
			print("Deadline reached, terminating.");
			sendViaIridium("ERROR: \"Deadline reached, terminating.\"", 60);
			sendViaSms("ERROR: \"Deadline reached, terminating.\"", 60);
			end();
		}
		else
			super.update(fref);
	}
	
	@Override
	protected void printFSMState() {
    	String method = currentThread().getStackTrace()[2].getMethodName();
        print("FSM State: " + method+" ("+((deadline.getTime() - System.currentTimeMillis()) / 1000)+" left)");
    }
	
	public void setDeadline(Date date) {
		this.deadline = date;
	}
}
