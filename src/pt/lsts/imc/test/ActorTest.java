package pt.lsts.imc.test;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.net.IMCQuery;

/**
 * Created by zp on 30-11-2016.
 */
public class ActorTest extends IMCActor {

    @Subscribe
    public void on(Message msg) {
        System.out.printf("%s from %s\n", msg.abbrev(), msg.src());
    }

    @Periodic(3000)
    public void periodic() {
        System.out.println(IMCQuery.q(EstimatedState.class).src("lauv-xplore-1").now());
    }

    public static void main(String args[]) {
        new ActorTest().run();
    }
}
