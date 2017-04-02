package pt.lsts.imc4j.runtime.state;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.grizzly.filterchain.BaseFilter;

import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EntityList;
import pt.lsts.imc4j.util.TupleList;

/**
 * This class holds a registry of known devices (previously announced),
 * including local info
 * 
 * @author zp
 */
public class IMCRegistry extends BaseFilter {

	private LinkedHashMap<String, Peer> peers = new LinkedHashMap<>();
	private LinkedHashMap<Integer, String> peerNames = new LinkedHashMap<>();
	private LinkedHashMap<Integer, TupleList> entities = new LinkedHashMap<>();	
	
	private String sys_name = System.getProperty("IMC_NAME", "IMC4J");
	private int local_id = Integer.decode(System.getProperty("IMC_ID", "0x3333"));
	private SystemType sys_type = SystemType.valueOf(System.getProperty("IMC_TYPE", "CCU"));
	
	private String services = "";
	private double lat = 0, lon = 0, height = 0;
	
	private static final Pattern pattern_udp = Pattern
			.compile("imc\\+udp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");
	private static final Pattern pattern_tcp = Pattern
			.compile("imc\\+tcp\\:\\/\\/(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\:(\\d+)/");

	/**
	 * Create an announce based on registered services
	 * @return An Announce message ready to send
	 */
	public Announce buildAnnounce() {
		Announce announce = new Announce();
		announce.sys_name = sys_name;
		announce.src = local_id;
		announce.services = services;
		announce.lat = lat;
		announce.lon = lon;
		announce.height = (float)height;
		announce.sys_type = sys_type;
		return announce;
	}
	
	/**
	 * @return Local IMC ID
	 */
	public int getImcId() {
		return local_id;
	}
	
	/**
	 * @return Local system name
	 */
	public String getSysName() {
		return sys_name;
	}
	
	/**
	 * @param id The local IMC id to announce
	 */
	public void setImcId(int id) {
		local_id = id;
	}
	
	/**
	 * @param name The local system name to announce
	 */
	public void setSysName(String name) {
		sys_name = name;
	}
	
	/**
	 * @param type The system type to announce
	 */
	public void setSysType(SystemType type) {
		sys_type = type;
	}
	
	/**
	 * Register a local service
	 * @param service A service to be added in upcoming announce messages
	 */
	public void addService(String service) {
		if (!services.isEmpty())
			services += ";";
		services += service;
	}
	
	/**
	 * Change the location of the local platform
	 * @param lat The latitude, in radians
	 * @param lon The longitude, in radians
	 * @param height The height, in meters
	 */
	public void setLocation(double lat, double lon, double height) {
		this.lat = lat;
		this.lon = lon;
		this.height = height;		
	}
	
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
	
	public List<String> peers() {
		updatePeers();
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
		
		return list.keyFor(src_ent);
	}
	
	/**
	 * Retrieve the id of an entity by name
	 * 
	 * @param src
	 *            The name of the source system
	 * @param entity
	 *            The name of the entity to resolve
	 * @return The id of the entity or <code>null</code> if unresolved.
	 */
	public Integer resolveEntity(String src, String entity) {
		Integer id = resolveSystem(src);
		if (id == null)
			return null;

		TupleList list = entities.get(id);
		if (list == null)
			return null;

		return list.getInt(entity);
	}
	
	public int registerLocalEntity(String name) {
		synchronized (entities) {
			TupleList local = entities.get(local_id);
			if (local == null) {
				local = new TupleList();
				entities.put(local_id, local);
			}
			int id = local.size();
			local.set(name, ""+id);
			return id;
		}
	}
	
	public TupleList getLocalEntities() {
		synchronized (entities) {
			return entities.get(local_id);
		}
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
