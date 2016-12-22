package pt.lsts.imc4j.net;

import java.util.ArrayList;
import java.util.HashSet;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.Heartbeat;
import pt.lsts.imc4j.runtime.ActorContext;
import pt.lsts.imc4j.runtime.IMCActor;

/**
 * This module will send IMC Heartbeats to all peers. A peer can be added by its
 * name or automatically by using autoconnect().
 * 
 * @see IMCBeater#addRecipient(String)
 * @see IMCBeater#setAutoConnect(boolean)
 * @author zp
 *
 */
public class IMCBeater extends IMCActor {

	public IMCBeater(ActorContext context) {
		super(context);
	}

	private HashSet<String> recipients = new HashSet<>();
	private boolean autoConnect = true;

	/**
	 * Retrieve current autoconnect mode.
	 * 
	 * @see #setAutoConnect(boolean)
	 */
	public final boolean isAutoConnect() {
		return autoConnect;
	}

	/**
	 * Whenever an announce arrives, start sending heartbeats to that source
	 */
	public final void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	@Periodic(1000)
	@Publish(Heartbeat.class)
	public void sendHeartbeat() {
		HashSet<String> destinations = new HashSet<>();
		synchronized (recipients) {
			destinations.addAll(recipients);
		}
		if (isAutoConnect())
			destinations.addAll(peers());
		
		for (String dst : destinations) 
			try {
				send(dst, new Heartbeat());
			} catch (Exception e) {

			}
	}

	/**
	 * Start sending heartbeats to given peer
	 * 
	 * @param peer
	 *            The name of the system (matching a received
	 *            {@link Announce#sys_name}).
	 */
	public void addRecipient(String peer) {
		synchronized (recipients) {
			recipients.add(peer);
		}
	}

	/**
	 * Remove a peer from list of peers.
	 * 
	 * @param peer
	 *            The peer to be removed
	 */
	public void removeRecipient(String peer) {
		synchronized (recipients) {
			recipients.remove(peer);
		}
	}

	/**
	 * Retrieve current list of peers
	 * 
	 * @return
	 */
	public ArrayList<String> getRecipients() {
		ArrayList<String> ret = new ArrayList<>();
		synchronized (recipients) {
			ret.addAll(recipients);
		}
		return ret;
	}

}
