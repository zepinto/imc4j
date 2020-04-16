package pt.lsts.backseat;

import java.util.Date;

import pt.lsts.imc4j.msg.FollowRefState;

public class TimedFSM extends FSMController {

	protected Date deadline = new Date(System.currentTimeMillis() + 3600 * 1000);
	
	@Override
	public void update(FollowRefState fref) {
		if (deadline != null && deadline.getTime() < System.currentTimeMillis()) {
			print("Deadline reached, terminating.");
			sendViaIridium("ERROR: \"Deadline reached, stopped.\"", 60);
			sendViaSms("ERROR: \"Deadline reached, stopped.\"", 60);
			setPaused(true);
		}
		else
			super.update(fref);
	}
	
	@Override
	protected void printFSMState() {
    	String method = currentThread().getStackTrace()[2].getMethodName();
        long timeMillis = deadline.getTime() - System.currentTimeMillis();
		//print("FSM State: " + method + " (" + (timeMillis / 1000)+"s left, or " + (timeMillis / 60000) + "min left, or " + (timeMillis / 3600000d) + "h left)");
		print(String.format("FSM State: %s (%ds left, or %dmin left, or %fh left)", method, timeMillis / 1000, timeMillis / 60000, timeMillis / 3600000d));
    }
	
	public void setDeadline(Date date) {
		this.deadline = date;
	}
}
