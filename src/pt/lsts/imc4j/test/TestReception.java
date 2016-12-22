package pt.lsts.imc4j.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.ActorContext;
import pt.lsts.imc4j.runtime.IMCActor;
import pt.lsts.imc4j.util.FormatConversion;

public class TestReception extends IMCActor {

	public TestReception(ActorContext context) {
		super(context);
	}
	
	@Subscribe
	public void on(Message msg) {
		System.out.println(FormatConversion.asJson(msg));
	}	
	
	public static void main(String[] args) throws Exception {
		IMCActor.exec(TestReception.class, ActorTest.class);
	}
}
