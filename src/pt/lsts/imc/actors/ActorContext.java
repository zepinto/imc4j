package pt.lsts.imc.actors;

import java.util.concurrent.Future;

import pt.lsts.imc.msg.Message;
import pt.lsts.imc.net.IMCRegistry;

public interface ActorContext {

	/**
	 * Post a message to this context
	 * @param msg The message to be posted
	 */
	public void post(Message msg);
	
	/**
	 * Send a message to an external destination (asynchronously)
	 * @param msg The message to send
	 * @param destination The name of the destination
	 */
	public void send(Message msg, String destination);
	
	/**
	 * Send a message reliably to destination
	 * @param msg The message to send
	 * @param destination Where to send the message
	 * @return A Future object that can be polled to check delivery result
	 */
	public Future<Boolean> deliver(Message msg, String destination);	
	
	/**
	 * Register an Actor in this context
	 * @param actor An actor
	 * @param name The name of the actor to be used in the context
	 * @return The entity id associated with the actor
	 */
	public int register(IMCActor actor, String name);
	
	public void unregister(IMCActor actor);
		
	/**
	 * @return Clock to use in this context
	 */
	public ActorClock clock();
	
	/**
	 * @return Registry used to resolve platform and entity names
	 */
	public IMCRegistry registry();
		
}
