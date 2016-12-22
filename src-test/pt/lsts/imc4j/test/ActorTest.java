package pt.lsts.imc4j.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;

/**
 * Created by zp on 30-11-2016.
 */
public class ActorTest extends AbstractActor {

	@Parameter
	int counter = 0;
	
	public ActorTest(ActorContext context) {
    	super(context);
	}
        
	@Subscribe    
    public void on(Message msg) {
    	System.out.printf("%s from %s\n", msg.abbrev(), systemName(msg.src));
    }

    @Periodic(5000)
    @Publish(Abort.class)
    public void periodic() throws Exception {
        System.out.println("Periodic "+ System.currentTimeMillis());
        send(new Abort());        
        System.out.println(peers());
    }
    
    @Periodic(1000)
    public void count() {
    	System.out.println(counter++);
    }
    
    @Override
    public void init() {    	
    	
    }

    public static void main(String args[]) throws Exception {
    	AbstractActor.exec(ActorTest.class);
    }
}
