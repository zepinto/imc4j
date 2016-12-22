package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.actors.ActorContext;
import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.annotations.Publish;
import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.msg.Message;

/**
 * Created by zp on 30-11-2016.
 */
public class ActorTest extends IMCActor {

	public ActorTest(ActorContext context) {
    	super(context);
	}
        
	@Subscribe    
    public void on(Message msg) {
    	System.out.printf("%s from %s\n", msg.abbrev(), systemName(msg.src));
    }

    @Periodic(3000)
    @Publish(EstimatedState.class)
    public void periodic() {
        
    }
    
    @Override
    public void init() {    	
    }

    public static void main(String args[]) throws Exception {
    	IMCActor.exec(ActorTest.class);
    }
}
