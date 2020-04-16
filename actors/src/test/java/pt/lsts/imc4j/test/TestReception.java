package pt.lsts.imc4j.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;
import pt.lsts.imc4j.util.FormatConversion;

public class TestReception extends AbstractActor {

	public TestReception(ActorContext context) {
		super(context);
	}
	
	@Subscribe
	public void on(Message msg) {
		System.out.println(FormatConversion.asJson(msg));
	}	
	
	public static void main(String[] args) throws Exception {
		AbstractActor.exec(TestReception.class, ActorTest.class);
	}
}
