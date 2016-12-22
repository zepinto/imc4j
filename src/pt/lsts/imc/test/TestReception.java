package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.actors.ActorContext;
import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.util.FormatConversion;

public class TestReception extends IMCActor {

	public TestReception(ActorContext context) {
		super(context);
	}
	
	@Subscribe
	public void on(Message msg) {
		System.out.println(FormatConversion.asJson(msg));
	}	
	
	public static void main(String[] args) throws Exception {
		IMCActor.exec(TestReception.class);
	}
}
