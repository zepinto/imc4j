package pt.lsts.imc4j.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;

/**
 * Created by zp on 30-11-2016.
 */
public class ActorTest extends AbstractActor {

	public ActorTest(ActorContext context) {
    	super(context);
	}
        
	@Subscribe    
    public void on(Message msg) {
		System.out.println(Thread.currentThread());
    	System.out.printf("%s from %s\n", msg.abbrev(), systemName(msg.src));
    }

    @Periodic(3000)
    @Publish(EstimatedState.class)
    public void periodic() {
    	System.out.println(Thread.currentThread());
        System.out.println("Periodic "+ System.currentTimeMillis());
    }
    
    @Override
    public void init() {    	
    	System.out.println(Thread.currentThread());
    }

    public static void main(String args[]) throws Exception {
    	AbstractActor.exec(ActorTest.class);
    }
}
