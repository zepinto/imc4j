package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.actors.ActorContext;
import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.annotations.Parameter;
import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.annotations.Publish;
import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.net.ImcContext;

/**
 * Created by zp on 30-11-2016.
 */
public class ActorTest extends IMCActor {

	@Parameter
	private String sys_name = "My name";

    @Parameter
    private int sys_id = 0x7777;

    private ActorContext context;
    
    @Subscribe    
    public void on(Message msg) {
        System.out.printf("%s from %s\n", msg.abbrev(), "");
    }

    @Periodic(3000)
    @Publish(EstimatedState.class)
    public void periodic() {
        
    }
    
    public ActorTest(ActorContext context) {
    	super(context);
    	this.context = context;
	}
    
    @Override
    public void init() {
    	context.registry().setSysName(sys_name);
    	context.registry().setImcId(sys_id);
    }

    public static void main(String args[]) throws Exception {
    	IMCActor.exec(ActorTest.class);
    }
}
