package pt.lsts.imc.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.nio.transport.UDPNIOConnection;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.util.NetworkUtils;
import pt.lsts.imc.util.PeriodicCallbacks;

public class IMCNetwork extends AbstractImcConnection {

	private static IMCNetwork instance = null;

	private Bus bus = new Bus(ThreadEnforcer.ANY);
	private IMCBeater beater = new IMCBeater();
	private IMCAnnouncer announcer = new IMCAnnouncer();
	private EntityListRequester entitiesReq = new EntityListRequester();
	private HashSet<Object> listeners = new HashSet<>();
	private FilterChain filterChain;
	private UDPNIOTransport udpTransport = null;
	private TCPNIOTransport tcpTransport = null;
	private UDPNIOConnection udpMulticast = null;

	private static synchronized IMCNetwork instance() {
		if (instance == null)
			instance = new IMCNetwork();
		return instance;
	}

	private IMCNetwork() {
		filterChain = IMCCodec.ImcFilter(this);		
	}
	
	private void _start() throws Exception {
		int udpPort = -1;
		int multicastPort = -1;
		for (int port = 6001; port < 6020; port++) {
			try {
				bindUdp(port);
				bindTcp(port);
				udpPort = port;
				break;
			} catch (Exception e) {
			}
		}
		if (udpPort == -1)
			throw new Exception("Could not bind to any port");

		Collection<String> netInt = NetworkUtils.getNetworkInterfaces();
		for (String itf : netInt) {
			IMCRegistry.addService("imc+udp://" + itf + ":" + udpPort + "/");
			IMCRegistry.addService("imc+tcp://" + itf + ":" + udpPort + "/");
		}

		for (int port = 30100; port < 30105; port++) {
			try {
				joinMulticastGroup("224.0.75.69", port);
				multicastPort = port;
				break;
			} catch (Exception e) {
			}
		}

		if (multicastPort == -1)
			throw new Exception("Could not join multicast group");

		register(beater);
		register(announcer);
		register(entitiesReq);
		
	}

	private void bindUdp(int port) throws IOException {
		if (udpTransport == null) {
			udpTransport = UDPNIOTransportBuilder.newInstance().build();
			udpTransport.setProcessor(filterChain);
			udpTransport.setReuseAddress(true);
			udpTransport.configureBlocking(false);
			udpTransport.start();
		}

		udpTransport.bind(port);
	}

	private void bindTcp(int port) throws IOException {
		if (tcpTransport == null) {
			tcpTransport = TCPNIOTransportBuilder.newInstance().build();
			tcpTransport.setProcessor(filterChain);
			tcpTransport.setReuseAddress(true);
			tcpTransport.configureBlocking(false);
			tcpTransport.start();
		}

		tcpTransport.bind(port);
	}

	private void joinMulticastGroup(String address, int port) throws Exception {
		if (udpTransport == null) {
			udpTransport = UDPNIOTransportBuilder.newInstance().build();
			udpTransport.setProcessor(filterChain);
			udpTransport.setReuseAddress(true);
			udpTransport.configureBlocking(false);
			udpTransport.start();
		}
		udpMulticast = (UDPNIOConnection) udpTransport.connect(null, new InetSocketAddress(port)).get(10,
				TimeUnit.SECONDS);
		InetAddress addr = Inet4Address.getByName(address);

		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface ni : Collections.list(nets)) {
			udpMulticast.join(addr, ni);
		}
	}

	private void fillIn(Message msg) {
		if (msg.src == 0xFFFF)
			msg.src = IMCRegistry.getImcId();
	}

	private void _stop() throws Exception {

		if (udpMulticast != null)
			udpMulticast.closeSilently();
		if (udpTransport != null)
			udpTransport.shutdownNow();
		if (tcpTransport != null)
			tcpTransport.shutdownNow();
		
		synchronized (instance().listeners) {
			for (Object o : instance().listeners) {
				unregister(o);				
			}
			instance().listeners.clear();
		}
	}

	private void _sendTcp(Message msg, InetSocketAddress addr) throws Exception {
		if (tcpTransport == null) {
			Socket socket = new Socket(addr.getAddress(), addr.getPort());
			socket.getOutputStream().write(msg.serialize());
			socket.close();
		} else {
			Connection<?> conn = tcpTransport.connect(addr).get(10, TimeUnit.SECONDS);
			conn.write(msg);
			conn.close();
		}
	}
	
	private void _sendUdp(Message msg, InetSocketAddress addr) throws Exception {
		if (udpTransport == null) {
			byte[] data = msg.serialize();
			DatagramPacket pkt = new DatagramPacket(data, data.length, addr);
			DatagramSocket dsocket = new DatagramSocket();
			dsocket.send(pkt);
			dsocket.close();
		} else {
			Connection<?> conn = udpTransport.connect(addr).get(10, TimeUnit.SECONDS);
			conn.write(msg);
			conn.close();
		}
	}
	
	private void _sendUdp(Message msg, String dst) throws Exception {
		Integer imcid = IMCRegistry.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = IMCRegistry.udpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over UDP");

		msg.dst = imcid;
		fillIn(msg);
		sendUdp(msg, addr);		
		bus.post(msg);
	}

	private void _sendTcp(Message msg, String dst) throws Exception {
		Integer imcid = IMCRegistry.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = IMCRegistry.tcpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over TCP");

		msg.dst = imcid;
		fillIn(msg);
		sendTcp(msg, addr);
		bus.post(msg);
	}
	
	private void _register(Object pojo) {
		synchronized (listeners) {
			if (listeners.add(pojo)) {
				bus.register(pojo);
				PeriodicCallbacks.register(pojo);
			}
		}
	}
	
	private void _unregister(Object pojo) {
		bus.unregister(pojo);
		PeriodicCallbacks.unregister(pojo);
		synchronized (listeners) {
			listeners.remove(pojo);
		}
	}
	
	private void _post(Object message) {
		bus.post(message);
	}
	
	/**
	 * Create IMC node and start listening for IMC messages 
	 */
	public static void start() throws Exception {
		instance()._start();
	}

	/**
	 * Stop listening for IMC messages
	 * @throws Exception
	 */
	public static void stop() throws Exception {
		instance()._stop();
	}

	/**
	 * Add a recipient for message and periodic events
	 * @param pojo An object with methods marked with {@linkplain Subscribe} and/or {@linkplain Periodic} 
	 */
	public static void register(Object pojo) {
		instance()._register(pojo);
	}

	/**
	 * Remove a previously registered recipient of events
	 * @param pojo The recipient to be removed
	 */
	public static void unregister(Object pojo) {
		instance()._unregister(pojo);		
	}

	/**
	 * Post an event to all recipients of that event type (class)
	 * @param msg An IMC message 
	 */
	public static void post(Message msg) {
		instance()._post(msg);
	}

	/**
	 * Send an IMC message to given UDP address
	 * @param msg The message to send
	 * @param addr The UDP endpoint
	 * @throws Exception In case the endpoint cannot be reached
	 */
	public static void sendUdp(Message msg, InetSocketAddress addr) throws Exception {
		instance()._sendUdp(msg, addr);
	}

	/**
	 * Send an IMC message to given TCP address
	 * @param msg The message to send
	 * @param addr The TCP endpoint
	 * @throws Exception In case the endpoint cannot be reached or transmission error
	 */
	public static void sendTcp(Message msg, InetSocketAddress addr) throws Exception {
		instance()._sendTcp(msg, addr);
	}
	
	/**
	 * Send an IMC message to the given system using UDP
	 * @param msg The message to send
	 * @param dst The name of the recipient (as announced)
	 * @throws Exception In case the system is not known
	 */
	public static void sendUdp(Message msg, String dst) throws Exception {
		instance()._sendUdp(msg, dst);
	}
	
	/**
	 * Send an IMC message to the given system using TCP
	 * @param msg The message to send
	 * @param dst The name of the recipient (as announced)
	 * @throws Exception In case the system is not known, it cannot be reached or transmission failed
	 */
	public static void sendTcp(Message msg, String dst) throws Exception {
		instance()._sendTcp(msg, dst);
	}
	
	/**
	 * Resolve the system name that generated the given message
	 * @param msg An IMC message
	 * @return The name of the system that generated the message
	 */
	public static String sourceName(Message msg) {
		return IMCRegistry.resolveSystem(msg.src);
	}

	/**
	 * Check if a system has transmitted anything recently
	 * @param system The name of the system
	 * @return <code>true</code> if the system sent some message in the last 60 seconds
	 */
	public static boolean isVisible(String system) {
		return IMCRegistry.isConnected(system);
	}

	/**
	 * If set to <code>true</code>, it will start sending heartbeats to all announced systems.
	 * @param autoconnect Whether to use Auto connect
	 */
	public static void setAutoconnect(boolean autoconnect) {
		instance().beater.setAutoConnect(autoconnect);
	}

	/**
	 * Send a message, via UDP, to all connected peers
	 * @param msg The message to send
	 */
	public static void sendToAll(Message msg) {
		for (String rec : instance().beater.getRecipients()) {
			try {
				sendUdp(msg, rec);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Synchronously receive a message from the network
	 * 
	 * @param abbrev
	 *            The type of message to receive
	 * @param timeout
	 *            The maximum time to wait for the message to arrive, in
	 *            milliseconds
	 * @return A first received message of that type or <code>null</code> if no
	 *         such message was received.
	 */
	public static Message recv(String abbrev, long timeout) {
		return instance().poll(abbrev, timeout);
	}
	
	/**
	 * Synchronously wait for a message coming from another peer
	 * 
	 * @param src
	 *            The name of the peer that should be sending the message
	 * @param abbrev
	 *            The type of message to receive
	 * @param timeout
	 *            How long to wait for the message
	 * @return The first received message of given type coming from specified
	 *         peer or <code>null</code> if the peer did not send that message
	 *         up to timeout milliseconds.
	 */
	public static Message recv(String src, String abbrev, long timeout) {
		return instance().poll(src, abbrev, timeout);
	}	
	
	@Override
	public void handleMessage(Message msg) {
		bus.post(msg);
	}
	
	@Override
	public void sendMessage(Message msg) {
		sendToAll(msg);
	}
}