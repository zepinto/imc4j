package pt.lsts.imc.net;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.util.TupleList;

/**
 * This class holds a registry of known devices (previously announced)
 * 
 * @author zp
 */
public class IMCRegistry {

	private LinkedHashMap<String, Peer> peers = new LinkedHashMap<>();
	private LinkedHashMap<Integer, String> peerNames = new LinkedHashMap<>();
	private LinkedHashMap<Integer, TupleList> entities = new LinkedHashMap<>();

	private static final Pattern pattern_udp = Pattern
			.compile("imc\\+udp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");
	private static final Pattern pattern_tcp = Pattern
			.compile("imc\\+tcp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");

	/**
	 * Set entities received
	 * 
	 * @param msg
	 *            Message with map of entities and ids
	 */
	public void setEntityList(EntityList msg) {
		entities.put(msg.src, msg.list);
	}

	/**
	 * Set received announce
	 * 
	 * @param announce
	 *            Announce message
	 * @param address
	 *            The Address where the message was sent from
	 */
	public void setAnnounce(Announce announce, InetSocketAddress address) {
		Peer peer = new Peer(announce, address);
		synchronized (peers) {
			peers.put(announce.sys_name, peer);
		}
		synchronized (peerNames) {
			peerNames.put(announce.src, announce.sys_name);
		}
	}

	/**
	 * Check if the system with given name has sent Announces recently
	 * 
	 * @param name
	 *            The name of the system
	 * @return <code>true</code> if the system has sent announce in the last 60
	 *         seconds
	 */
	public boolean isConnected(String name) {
		synchronized (peers) {
			updatePeers();
			return peers.containsKey(name);
		}
	}

	private void updatePeers() {
		synchronized (peers) {
			Iterator<Entry<String, Peer>> entries = peers.entrySet().iterator();
			while (entries.hasNext()) {
				Entry<String, Peer> entry = entries.next();
				if (entry.getValue().ageMillis() > 60000)
					entries.remove();
			}
		}
	}

	/**
	 * Retrieve the IMC id of a system name
	 * 
	 * @param name
	 *            The name of the system to resolve
	 * @return The IMC ID of the system or <code>null</code> if the system is
	 *         not connected.
	 */
	public Integer resolveSystem(String name) {
		Peer p;
		synchronized (peers) {
			p = peers.get(name);
		}

		return p != null ? p.id : null;
	}

	/**
	 * Retrieve the name of a system given its ID
	 * 
	 * @param id
	 *            The IMC id of the system to look for
	 * @return The name of the system or <code>null</code> if the system is not
	 *         connected.
	 */
	public String resolveSystem(int id) {
		synchronized (peerNames) {
			return peerNames.get(id);
		}
	}

	/**
	 * Lookup an UDP address for given system
	 * 
	 * @param name
	 *            The name of the system to resolve
	 * @return The UDP address of the system or <code>null</code> if the system
	 *         did not announce UDP.
	 */
	public InetSocketAddress udpAddress(String name) {
		Peer p;
		synchronized (peers) {
			p = peers.get(name);
		}
		return p != null ? p.udpAddress : null;
	}

	/**
	 * Lookup a TCP address for given system
	 * 
	 * @param name
	 *            The name of the system to resolve
	 * @return The TCP address of the system or <code>null</code> if the system
	 *         did not announce TCP.
	 */
	public InetSocketAddress tcpAddress(String name) {
		Peer p;
		synchronized (peers) {
			p = peers.get(name);
		}
		return p != null ? p.tcpAddress : null;
	}

	/**
	 * Retrieve the name of an entity by ID
	 * 
	 * @param src
	 *            The ID of the system
	 * @param src_ent
	 *            The ID of the entity
	 * @return The name of the entity with given id at the given system
	 */
	public String resolveEntity(int src, int src_ent) {
		TupleList list = entities.get(src);
		if (list == null)
			return null;
		return list.get("" + src_ent);
	}

	private static class Peer {
		int id;

		InetSocketAddress udpAddress = null, tcpAddress = null;
		long timestamp = System.currentTimeMillis();

		Peer(Announce ann, InetSocketAddress source) {
			id = ann.src;

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

		long ageMillis() {
			return System.currentTimeMillis() - timestamp;
		}
	}
}
