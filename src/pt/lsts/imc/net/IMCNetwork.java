package pt.lsts.imc.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.nio.transport.UDPNIOConnection;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.util.PeriodicCallbacks;

public class IMCNetwork extends BaseFilter {

	private static String SystemName = "IMC4J";
	private static int ImcId = 0x3333;
	private static IMCNetwork instance = null;

	private Bus bus = new Bus(ThreadEnforcer.ANY);
	private IMCPeers peers = new IMCPeers();
	private IMCBeater beater = new IMCBeater();
	private IMCAnnouncer announcer;
	private HashSet<Object> listeners = new HashSet<>();
	private FilterChainBuilder filterChainBuilder;
	private UDPNIOTransport udpTransport = null;
	private TCPNIOTransport tcpTransport = null;
	private UDPNIOConnection udpMulticast = null;

	private static synchronized IMCNetwork instance() {
		if (instance == null)
			instance = new IMCNetwork();
		return instance;
	}

	private IMCNetwork() {
		filterChainBuilder = FilterChainBuilder.stateless();
		filterChainBuilder.add(new TransportFilter());
		filterChainBuilder.add(new IMCCodec());
		filterChainBuilder.add(this);
	}

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Message msg = ctx.getMessage();
		if (msg instanceof Announce)
			peers.setAnnounce((Announce) msg, (InetSocketAddress) ctx.getAddress());
		bus.post(msg);
		return ctx.getStopAction();
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

		announcer = new IMCAnnouncer(SystemName, ImcId, udpPort);
		register(beater);
		register(announcer);
	}

	private void bindUdp(int port) throws IOException {
		if (udpTransport == null) {
			udpTransport = UDPNIOTransportBuilder.newInstance().build();
			udpTransport.setProcessor(filterChainBuilder.build());
			udpTransport.setReuseAddress(true);
			udpTransport.configureBlocking(false);
			udpTransport.start();
		}

		udpTransport.bind(port);
	}

	private void bindTcp(int port) throws IOException {
		if (tcpTransport == null) {
			tcpTransport = TCPNIOTransportBuilder.newInstance().build();
			tcpTransport.setProcessor(filterChainBuilder.build());
			tcpTransport.setReuseAddress(true);
			tcpTransport.configureBlocking(false);
			tcpTransport.start();
		}

		tcpTransport.bind(port);
	}

	private void joinMulticastGroup(String address, int port) throws Exception {
		if (udpTransport == null) {
			udpTransport = UDPNIOTransportBuilder.newInstance().build();
			udpTransport.setProcessor(filterChainBuilder.build());
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
			msg.src = ImcId;
	}

	private void _stop() throws Exception {

		if (udpMulticast != null)
			udpMulticast.closeSilently();
		if (udpTransport != null)
			udpTransport.shutdownNow();
		if (tcpTransport != null)
			tcpTransport.shutdownNow();

		synchronized (instance().listeners) {
			for (Object o : instance().listeners)
				unregister(o);
			instance().listeners.clear();
		}
	}

	/* STATIC METHODS */

	public static void sendUdp(Message msg, InetSocketAddress addr) throws Exception {
		if (instance().udpTransport == null) {
			byte[] data = msg.serialize();
			DatagramPacket pkt = new DatagramPacket(data, data.length, addr);
			DatagramSocket dsocket = new DatagramSocket();
			dsocket.send(pkt);
			dsocket.close();
		} else {
			Connection<?> conn = instance().udpTransport.connect(addr).get(10, TimeUnit.SECONDS);
			conn.write(msg);
			conn.close();
		}
	}

	private static void sendTcp(Message msg, InetSocketAddress addr) throws Exception {
		if (instance().tcpTransport == null) {
			Socket socket = new Socket(addr.getAddress(), addr.getPort());
			socket.getOutputStream().write(msg.serialize());
			socket.close();
		} else {
			Connection<?> conn = instance().tcpTransport.connect(addr).get(10, TimeUnit.SECONDS);
			conn.write(msg);
			conn.close();
		}
	}

	public static void sendUdp(Message msg, String dst) throws Exception {
		Integer imcid = instance().peers.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = instance().peers.udpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over UDP");

		msg.dst = imcid;
		instance().fillIn(msg);
		sendUdp(msg, addr);
		instance().bus.post(msg);
	}

	public static void sendTcp(Message msg, String dst) throws Exception {
		Integer imcid = instance().peers.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = instance().peers.tcpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over TCP");

		msg.dst = imcid;
		instance().fillIn(msg);
		sendTcp(msg, addr);
		instance().bus.post(msg);
	}

	public static void start() throws Exception {
		instance()._start();
	}

	public static void stop() throws Exception {
		PeriodicCallbacks.stopAll();
		instance()._stop();

	}

	public static void register(Object pojo) {
		synchronized (instance().listeners) {
			if (instance().listeners.add(pojo)) {
				instance().bus.register(pojo);
				PeriodicCallbacks.register(pojo);
			}
		}
	}

	public static void unregister(Object pojo) {
		instance().bus.unregister(pojo);
		PeriodicCallbacks.unregister(pojo);
		synchronized (instance().listeners) {
			instance().listeners.remove(pojo);
		}
	}

	public static void post(Message msg) {
		instance().bus.post(msg);
	}

	public static String sourceName(Message msg) {
		return instance().peers.resolveSystem(msg.src);
	}

	public static boolean isVisible(String system) {
		return instance().peers.isConnected(system);
	}

	public static void setAutoconnect(boolean autoconnect) {
		instance().beater.setAutoConnect(autoconnect);
	}

	public static void sendToAll(Message msg) {
		for (String rec : instance().beater.getRecipients()) {
			try {
				sendUdp(msg, rec);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws Exception {
		IMCNetwork.start();
		System.out.println("Press any key to stop the server...");
		System.in.read();
		IMCNetwork.stop();
	}
}