package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.msg.Message;
import pt.lsts.imc.net.IMCNetwork;
import pt.lsts.imc.util.FormatConversion;

public class TestReception {

	@Subscribe
	public void on(Message msg) {
		System.out.println(FormatConversion.asJson(msg));
	}	
	
	public static void main(String[] args) throws Exception {
		TestReception rec = new TestReception();
		IMCNetwork.register(rec);
		IMCNetwork.start();
	}
}
