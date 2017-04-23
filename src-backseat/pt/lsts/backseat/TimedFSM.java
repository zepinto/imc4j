package pt.lsts.backseat;

import java.util.Date;

import pt.lsts.imc4j.msg.FollowRefState;

public class TimedFSM extends FSMController {

	protected Date deadline = new Date(System.currentTimeMillis() + 3600 * 1000);
	
	@Override
	public void update(FollowRefState fref) {
		if (deadline.getTime() < System.currentTimeMillis()) {
			print("Deadline reached, terminating.");
			end();
		}
		else
			super.update(fref);
	}
}
