package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.annotations.Parameter;
import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.msg.Loiter;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.msg.PlanControl;
import pt.lsts.imc.net.IMCQuery;
import pt.lsts.imc.net.IMCRegistry;
import pt.lsts.imc.util.PojoConfig;

import java.util.EnumSet;

/**
 * Created by zp on 30-11-2016.
 */
public class ActorTest extends IMCActor {

	@Parameter
	private String sys_name = IMCRegistry.getSysName();

    @Parameter
    private int sys_id = IMCRegistry.getImcId();


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
    	IMCRegistry.setSysName(sys_name);
        IMCRegistry.setImcId(sys_id);
    }

    public static void main(String args[]) throws Exception {
    	
    	if (args.length == 0)
    		args = new String[] {"--sys_name=My Name", "--x=y"};
    	
    	new ActorTest().run(PojoConfig.asProperties(args));


        PlanControl m = new PlanControl();
        m.type = PlanControl.TYPE.PC_REQUEST;
        m.op = PlanControl.OP.PC_START;
        m.request_id = 38;
        m.flags = EnumSet.of(PlanControl.FLAGS.FLG_CALIBRATE);

        m.arg = new Loiter();
        ((Loiter)m.arg).lat = 60.0;
        ((Loiter)m.arg).lon = -8.0;
        ((Loiter)m.arg).radius = 30f;
        ((Loiter)m.arg).type = Loiter.TYPE.LT_CIRCULAR;
        ((Loiter)m.arg).z_units = ZUnits.DEPTH;
        ((Loiter)m.arg).z = 2f;
        ((Loiter)m.arg).speed = 1.5f;
        ((Loiter)m.arg).speed_units = SpeedUnits.METERS_PS;
        m.info = "Start this "+m.arg.abbrev();

        System.out.println(m);

    }
}
