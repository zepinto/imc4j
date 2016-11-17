package pt.lsts.imc.net;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;

public class IMCPeers {

	private LinkedHashMap<String, Peer> peers = new LinkedHashMap<>();
	private LinkedHashMap<Integer, String> peerNames = new LinkedHashMap<>();
	private LinkedHashMap<Integer, EntityList> entities = new LinkedHashMap<>();
	
	private static final Pattern pattern_udp = Pattern
			.compile("imc\\+udp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");
	private static final Pattern pattern_tcp = Pattern
			.compile("imc\\+tcp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");

	public void setEntityList(EntityList msg) {
		entities.put(msg.src, msg);
	}
	
	public void setAnnounce(Announce announce, InetSocketAddress address) {
		Peer peer = new Peer(announce, address);
		synchronized (peers) {
			peers.put(announce.sys_name, peer);			
		}
		synchronized (peerNames) {
			peerNames.put(announce.src, announce.sys_name);		
		}	
	}
	
	public boolean isConnected(String name) {
		synchronized (peers) {
			updatePeers();
			return peers.containsKey(name);
		}
	}
	
	public void updatePeers() {
		synchronized (peers) {
			Iterator<Entry<String, Peer>> entries = peers.entrySet().iterator();
			while(entries.hasNext()) {
				Entry<String, Peer> entry = entries.next();
				if (entry.getValue().ageMillis() > 60000)
					entries.remove();
			}			
		}
	}
	
	public Integer resolveSystem(String name) {
		Peer p;
		synchronized (peers) {
			 p = peers.get(name);	
		}
		
		return p != null? p.id : null; 		
	}
	
	public String resolveSystem(int id) {
		synchronized (peerNames) {
			return peerNames.get(id);
		}
	}
	
	public InetSocketAddress udpAddress(String name) {
		Peer p;
		synchronized (peers) {
			 p = peers.get(name);	
		}
		return p != null? p.udpAddress : null; 	
	}
	
	public InetSocketAddress tcpAddress(String name) {
		Peer p;
		synchronized (peers) {
			 p = peers.get(name);	
		}
		return p != null? p.tcpAddress : null; 	
	}
	
	public String resolveEntity(String system, String entity) {
		int id = resolveSystem(system);
		EntityList msg = entities.get(id);
		if (msg == null)
			return null;
		return msg.list.get(entity);
	}
	
	static class Peer {
		int id;
		String name;
		
		InetSocketAddress udpAddress = null, tcpAddress = null;
		long timestamp = System.currentTimeMillis();

		public Peer(Announce ann, InetSocketAddress source) {
			id = ann.src;
			name = ann.sys_name;
			String[] services = ann.services.split(";");
			
			String address = source.getAddress().getHostAddress();

			for (String serv : services) {
				Matcher mUdp = pattern_udp.matcher(serv);
				if (mUdp.matches()) {
					udpAddress = new InetSocketAddress(address, Integer.parseInt(mUdp.group(5)));
				}

				Matcher mTcp = pattern_tcp.matcher(serv);
				if (mTcp.matches()) {
					tcpAddress = new InetSocketAddress(address, Integer.parseInt(mTcp.group(5)));					
				}
			}
		}
		
		public long ageMillis() {
			return System.currentTimeMillis() - timestamp;
		}
	}
}
