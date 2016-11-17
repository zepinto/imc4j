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

import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.def.SystemType;
import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.util.NetworkUtils;
import pt.lsts.imc.util.PeriodicCallbacks;

public class IMCNetwork extends BaseFilter {

	private static IMCNetwork instance = null;
	private Bus bus = new Bus(ThreadEnforcer.ANY);
	private IMCPeers peers = new IMCPeers();
	private FilterChainBuilder filterChainBuilder;
	private UDPNIOTransport udpTransport = null;
	private TCPNIOTransport tcpTransport = null;	
	private UDPNIOConnection udpMulticast = null;	
	private int imcId = 0x3333, udpPort = -1, multicastPort = -1;
	private String imcName = "imc4j";
	private static final String multicastAddress = "224.0.75.69"; 
	
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
			peers.setAnnounce((Announce)msg, (InetSocketAddress)ctx.getAddress());
		bus.post(msg);
		return ctx.getStopAction();
	}
	
	public static synchronized IMCNetwork instance() {
		if (instance == null)
			instance = new IMCNetwork();
		return instance;
	}
	
	@Periodic(10000)
	private void sendAnnounce() {
		Announce announce = buildAnnounce();
		for (int port = 30100; port < 30105; port++) {
			try {
				sendUdp(announce, new InetSocketAddress(multicastAddress, port));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}		
	}
	
	private Announce buildAnnounce() {
		Announce announce = new Announce();
		announce.src = imcId;
		announce.sys_name = imcName;
		announce.sys_type = SystemType.CCU;
		announce.services = "";

		Collection<String> netInt = NetworkUtils.getNetworkInterfaces();
		for (String itf : netInt) {
			announce.services += "imc+udp://" + itf + ":" + udpPort + "/;";
			announce.services += "imc+tcp://" + itf + ":" + udpPort + "/;";
		}
		if (announce.services.length() > 0)
			announce.services = announce.services.substring(0, announce.services.length() - 1);
		
		return announce;
	}
	
	private void _start() throws Exception {
		for (int port = 6001; port < 6020; port++) {
			try {
				bindUdp(port);
				bindTcp(port);
				udpPort = port;
				break;
			}
			catch (Exception e) {
			}
		}
		if (udpPort == -1)
			throw new Exception("Could not bind to any port"); 
		
		for (int port = 30100; port < 30105; port++) {
			try {
				joinMulticastGroup("224.0.75.69", port);
				multicastPort = port;
				break;
			}
			catch (Exception e) {
			}
		}
		
		if (multicastPort == -1)
			throw new Exception("Could not join multicast group");
		
		sendAnnounce();
		PeriodicCallbacks.register(this);
	}
	
	public static void start() throws Exception {
		instance()._start();		
	}
	
	public void bindUdp(int port) throws IOException {
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
		udpMulticast = (UDPNIOConnection) udpTransport.connect(null, new InetSocketAddress(port)).get(10, TimeUnit.SECONDS);
		InetAddress addr = Inet4Address.getByName(address);
		
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface ni : Collections.list(nets)) {
        	udpMulticast.join(addr, ni);
        }
	}
	
	private void fillIn(Message msg) {
		if (msg.src == 0xFFFF)
			msg.src = imcId;		
	}
	
	public static void stop() throws Exception {
		PeriodicCallbacks.stopAll();
		instance().shutdown();		
	}
	
	private void shutdown() throws Exception {
		if (udpMulticast != null)
			udpMulticast.closeSilently();
		if (udpTransport != null)
			udpTransport.shutdownNow();
		if (tcpTransport != null)
			tcpTransport.shutdownNow();		
	}
	
	public void sendUdp(Message msg, InetSocketAddress addr) throws Exception {
		if (udpTransport == null) {
			byte[] data = msg.serialize();	
			DatagramPacket pkt = new DatagramPacket(data, data.length, addr);
			DatagramSocket dsocket = new DatagramSocket();
			dsocket.send(pkt);
			dsocket.close();		
		}
		else {
			Connection<?> conn = udpTransport.connect(addr).get(10, TimeUnit.SECONDS);
			conn.write(msg);
			conn.close();
		}		
	}
	
	public void sendTcp(Message msg, InetSocketAddress addr) throws Exception {
		if (tcpTransport == null) {
			Socket socket = new Socket(addr.getAddress(), addr.getPort());
			socket.getOutputStream().write(msg.serialize());
			socket.close();		
		}
		else {
			Connection<?> conn = tcpTransport.connect(addr).get(10, TimeUnit.SECONDS);
			conn.write(msg);
			conn.close();			
		}		
	}
		
	public static void register(Object pojo) {
		instance().bus.register(pojo);
		PeriodicCallbacks.register(pojo);
	}
	
	public static void unregister(Object pojo) {
		instance().bus.unregister(pojo);
		PeriodicCallbacks.unregister(pojo);
	}	

	public void sendUdp(Message msg, String dst) throws Exception {
		Integer imcid = peers.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");
		
		InetSocketAddress addr = peers.udpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over UDP");
		
		msg.dst = imcid;
		fillIn(msg);
		sendUdp(msg, addr);
		bus.post(msg);
	}
	
	public void sendTcp(Message msg, String dst) throws Exception {
		Integer imcid = peers.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");
		
		InetSocketAddress addr = peers.tcpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over TCP");
		
		msg.dst = imcid;
		fillIn(msg);
		sendTcp(msg, addr);
		bus.post(msg);
	}
	
	public static void post(Message msg) {
		instance().bus.post(msg);
	}
	
	public boolean isVisible(String system) {
		return instance().peers.isConnected(system);
	}
	
	public static void main(String[] args) throws Exception {
		IMCNetwork.start();
        System.out.println("Press any key to stop the server...");
        System.in.read();        
        IMCNetwork.stop();
	}	
}