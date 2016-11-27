package pt.lsts.imc.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import pt.lsts.imc.def.SystemType;
import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.util.TupleList;

/**
 * This class holds a registry of known devices (previously announced),
 * including local info
 * 
 * @author zp
 */
public class IMCRegistry extends BaseFilter {

	private static IMCRegistry instance = null;
	
	public static synchronized IMCRegistry instance() {
		if (instance == null)
			instance = new IMCRegistry();
		return instance;
	}
	
	private LinkedHashMap<String, Peer> peers = new LinkedHashMap<>();
	private LinkedHashMap<Integer, String> peerNames = new LinkedHashMap<>();
	private LinkedHashMap<Integer, TupleList> entities = new LinkedHashMap<>();
	
	private String sys_name = "IMC4J";
	private int local_id = 0x3333;
	private String services = "";
	private double lat = 0, lon = 0, height = 0;
	private SystemType sys_type = SystemType.CCU;
	
	private static final Pattern pattern_udp = Pattern
			.compile("imc\\+udp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");
	private static final Pattern pattern_tcp = Pattern
			.compile("imc\\+tcp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");

	public static Announce buildAnnounce() {
		IMCRegistry reg = instance();
		Announce announce = new Announce();
		announce.sys_name = reg.sys_name;
		announce.src = reg.local_id;
		announce.services = reg.services;
		announce.lat = reg.lat;
		announce.lon = reg.lon;
		announce.height = (float)reg.height;
		announce.sys_type = reg.sys_type;
		return announce;
	}
	
	public static void setAnnounce(Announce announce, InetSocketAddress address) {
		instance()._setAnnounce(announce, address);
	}
	
	public static void setEntityList(EntityList list) {
		instance()._setEntityList(list);
	}
	
	public static int getImcId() {
		return instance().local_id;
	}
	
	public static String getSysName() {
		return instance().sys_name;
	}
	
	public static void setImcId(int id) {
		instance().local_id = id;
	}
	
	public static void setSysName(String name) {
		instance().sys_name = name;
	}
	
	public static void setSysType(SystemType type) {
		instance().sys_type = type;
	}
	
	public static void addService(String service) {
		IMCRegistry registry = instance();
		if (!registry.services.isEmpty())
			registry.services += ";";
		registry.services += service;
	}
	
	public static void setLocation(double lat, double lon, double height) {
		IMCRegistry registry = instance();
		registry.lat = lat;
		registry.lon = lon;
		registry.height = height;		
	}
	
	public static Collection<String> connectedPeers() {
		return instance()._peers();
	}
	
	public static boolean isConnected(String name) {
		return instance()._isConnected(name);
	}

	public static Integer resolveSystem(String name) {
		return instance()._resolveSystem(name);
	}

	public static String resolveSystem(int id) {
		return instance()._resolveSystem(id);
	}

	public static InetSocketAddress udpAddress(String name) {
		return instance()._udpAddress(name);
	}

	public static InetSocketAddress tcpAddress(String name) {
		return instance()._tcpAddress(name);
	}

	public static String resolveEntity(int src, int src_ent) {
		return instance()._resolveEntity(src, src_ent);
	}
	
	/**
	 * Set entities received
	 * 
	 * @param msg
	 *            Message with map of entities and ids
	 */
	private void _setEntityList(EntityList msg) {
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
	private void _setAnnounce(Announce announce, InetSocketAddress address) {
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
	private boolean _isConnected(String name) {
		synchronized (peers) {
			updatePeers();
			return peers.containsKey(name);
		}
	}
	
	private List<String> _peers() {
		ArrayList<String> ret = new ArrayList<>();
		synchronized (peers) {
			ret.addAll(peers.keySet());
		}
		ret.remove(getSysName());
		return ret;
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
	private Integer _resolveSystem(String name) {
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
	private String _resolveSystem(int id) {
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
	private InetSocketAddress _udpAddress(String name) {
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
	private InetSocketAddress _tcpAddress(String name) {
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
	private String _resolveEntity(int src, int src_ent) {
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
	
	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		return super.handleRead(ctx);
	}
}
