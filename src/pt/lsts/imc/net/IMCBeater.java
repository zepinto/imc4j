package pt.lsts.imc.net;

import java.util.ArrayList;
import java.util.HashSet;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.Heartbeat;

public class IMCBeater {

	private HashSet<String> recipients = new HashSet<>();
	private boolean autoConnect = true;
	
	public final boolean isAutoConnect() {
		return autoConnect;
	}

	public final void setAutoConnect(boolean autoConnect) {
		this.autoConnect = autoConnect;
	}

	@Periodic(1000)
	public void sendHeartbeat() {
		synchronized (recipients) {
			for (String dst : recipients)
				try {
					IMCNetwork.sendUdp(new Heartbeat(), dst);
				} catch (Exception e) {
					
				}
		}
	}
	
	public void addPeer(String peer) {
		synchronized (recipients) {
			recipients.add(peer);
		}
	}
	
	public void removePeer(String peer) {
		synchronized (recipients) {
			recipients.remove(peer);
		}
	}
	
	public ArrayList<String> getRecipients() {
		ArrayList<String> ret = new ArrayList<>();
		synchronized (recipients) {
			ret.addAll(recipients);
		}		
		return ret;
	}
	
	@Subscribe
	public void on(Announce ann) {
		if (isAutoConnect())
			addPeer(ann.sys_name);
	}
}
