package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.net.IMCNetwork;

public class TestReception {

	@Subscribe
	public void on(EstimatedState state) {
		System.out.println("STATE from "+IMCNetwork.sourceName(state));
	}
	
	@Subscribe
	public void on(Announce msg) {
		System.out.println("ANNOUNCE from "+IMCNetwork.sourceName(msg));
	}	
	
	public static void main(String[] args) throws Exception {
		TestReception rec = new TestReception();
		IMCNetwork.register(rec);
		IMCNetwork.start();
	}
}
