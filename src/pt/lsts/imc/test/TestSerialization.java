package pt.lsts.imc.test;

import pt.lsts.imc.msg.Message;
import pt.lsts.imc.msg.PlanControl;

public class TestSerialization {
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		
		for (int i = 0; i < 1000000; i++) {
			PlanControl pc = new PlanControl();
			Message.deserialize(pc.serialize());
		}
		
		System.out.println(System.currentTimeMillis() - time);
		
		
	}
}
