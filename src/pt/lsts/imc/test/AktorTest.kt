package pt.lsts.imc.test

import com.squareup.otto.Subscribe
import pt.lsts.imc.actors.IMCActor
import pt.lsts.imc.annotations.Periodic
import pt.lsts.imc.def.SpeedUnits
import pt.lsts.imc.def.ZUnits
import pt.lsts.imc.msg.*
import pt.lsts.imc.net.IMCQuery
import java.util.*

fun <T : Message> msg(msg: Class<T>, builder: T.() -> Unit): T {
    val m = MessageFactory.create(msg.simpleName) as T
    m.builder()
    return m;
}

class KotlinTest : IMCActor() {

    @Subscribe
    fun on(msg: Message) {
        println("${msg.abbrev()} from ${msg.src()}")
    }

    @Periodic(3.times(1000))
    fun periodic() {
        println(IMCQuery.q(EstimatedState::class.java).src("lauv-xplore-1").now())
    }
}

fun main(args : Array<String>) {
    KotlinTest().run()

    val m = msg(PlanControl::class.java) {
        type = PlanControl.TYPE.PC_REQUEST
        op = PlanControl.OP.PC_START
        request_id = 38
        flags = EnumSet.of(PlanControl.FLAGS.FLG_CALIBRATE)
        arg = msg(Loiter::class.java) {
            lat = 60.0
            lon = -8.0
            radius = 30f
            type = Loiter.TYPE.LT_CIRCULAR
            z_units = ZUnits.DEPTH
            z = 2f
            speed = 1.5f
            speed_units = SpeedUnits.METERS_PS
        }
        info = "Start this ${arg.abbrev()}"
    }

    println (m)
}
