package pt.lsts.imc.actors

import com.squareup.otto.Subscribe
import pt.lsts.imc.annotations.Periodic
import pt.lsts.imc.msg.EntityList
import pt.lsts.imc.msg.EstimatedState
import pt.lsts.imc.msg.Message
import pt.lsts.imc.net.IMCNetwork
import pt.lsts.imc.net.IMCRegistry

/**
 * Mix-in a method to return the source of a message as String
 * @return The source of this message or <code>unknown</code> if the source hasn't sent an announce yet
 */
fun Message.srcName(): String = IMCRegistry.resolveSystem(src) ?: "unknown!?"

/**
 * Mix-in a method to return the name of a message
 * @return The name of this message (matches the 'abbrev' in IMC definition)
 */
fun Message.abbrev() : String = javaClass.simpleName

class KotlinTest : IMCActor() {

    @Subscribe
    fun on(msg: Message) {
        println("${msg.abbrev()} from ${msg.srcName()}")
    }

    @Periodic(3.times(1000))
    fun periodic() {
        println("3 seconds have passed")
        println(send("lauv-xplore-1", EntityList()))
    }
}

fun main(args : Array<String>) {
    KotlinTest().run()
}

