package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.msg.EntityList.OP;
import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.net.IMCNetwork;
import pt.lsts.imc.net.IMCRegistry;

public class TestReception {

	@Subscribe
	public void on(EstimatedState state) {
		System.out.println("STATE from "+IMCNetwork.sourceName(state));
	}
	
	@Subscribe
	public void on(Announce msg) {
		System.out.println("ANNOUNCE from "+IMCNetwork.sourceName(msg));
	}	
	
	@Subscribe
	public void on(EntityList msg) {
		if (msg.src != IMCRegistry.getImcId() && msg.op != OP.OP_QUERY)
			System.out.println("Entities from "+IMCNetwork.sourceName(msg));
	}	
	
	
	public static void main(String[] args) throws Exception {
		TestReception rec = new TestReception();
		IMCNetwork.register(rec);
		IMCNetwork.start();
		
		Thread.sleep(25000);
		
		System.out.println(IMCRegistry.resolveEntity(26, 13));
		System.out.println(IMCRegistry.resolveEntity("lauv-xplore-2", "Navigation"));
	}
}
