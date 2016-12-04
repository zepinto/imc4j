package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.annotations.Parameter;
import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.net.IMCQuery;
import pt.lsts.imc.net.IMCRegistry;
import pt.lsts.imc.util.PojoConfig;

/**
 * Created by zp on 30-11-2016.
 */
public class ActorTest extends IMCActor {

	@Parameter
	private String name = "CCU-IMC4J";
	
    @Subscribe
    public void on(Message msg) {
        System.out.printf("%s from %s\n", msg.abbrev(), msg.src());
    }

    @Periodic(3000)
    public void periodic() {
        System.out.println(IMCQuery.q(EstimatedState.class).src("lauv-xplore-1").now());
    }
    
    @Override
    public void init() {
    	IMCRegistry.setSysName(name);
    }

    public static void main(String args[]) throws Exception {
    	
    	if (args.length == 0)
    		args = new String[] {"--name=My Name", "--x=y"};
    	
    	new ActorTest().run(PojoConfig.asProperties(args));
    }
}
