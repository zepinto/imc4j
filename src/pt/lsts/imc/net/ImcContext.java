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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.nio.transport.UDPNIOConnection;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import pt.lsts.imc.actors.ActorClock;
import pt.lsts.imc.actors.ActorContext;
import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.actors.RealTimeClock;
import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.util.NetworkUtils;
import pt.lsts.imc.util.PeriodicCallbacks;

public class ImcContext extends BaseFilter implements ActorContext {

	private Bus bus = new Bus(ThreadEnforcer.ANY);
	private HashSet<Object> listeners = new HashSet<>();
	private FilterChain filterChain;
	private UDPNIOTransport udpTransport = null;
	private TCPNIOTransport tcpTransport = null;
	private UDPNIOConnection udpMulticast = null;
	private IMCRegistry registry = new IMCRegistry();
	private RealTimeClock clock = new RealTimeClock();
	private ExecutorService executor = Executors.newFixedThreadPool(3);
	
	public ImcContext() {
		filterChain = IMCCodec.ImcFilter(this);
		new IMCBeater(this);
		new IMCAnnouncer(this);
		new EntityListRequester(this);
		new IMCState(this);
				
	}
	
	public void start() throws Exception {
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
			registry.addService("imc+udp://" + itf + ":" + udpPort + "/");
			registry.addService("imc+tcp://" + itf + ":" + udpPort + "/");
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
			msg.src = registry.getImcId();
	}

	public void stop() throws Exception {

		if (udpMulticast != null)
			udpMulticast.closeSilently();
		if (udpTransport != null)
			udpTransport.shutdownNow();
		if (tcpTransport != null)
			tcpTransport.shutdownNow();
		
		synchronized (listeners) {
			for (Object o : listeners) {
				unregister(o);				
			}
			listeners.clear();
		}
	}

	private void sendTcp(Message msg, InetSocketAddress addr) throws Exception {
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
	
	private void sendUdp(Message msg, InetSocketAddress addr) throws Exception {
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
	
	public void sendUdp(Message msg, String dst) throws Exception {
		Integer imcid = registry.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = registry.udpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over UDP");

		msg.dst = imcid;
		fillIn(msg);
		sendUdp(msg, addr);		
		bus.post(msg);
	}

	public void sendTcp(Message msg, String dst) throws Exception {
		Integer imcid = registry.resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = registry.tcpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over TCP");

		msg.dst = imcid;
		fillIn(msg);
		sendTcp(msg, addr);
		bus.post(msg);
	}
	
	public void unregister(IMCActor pojo) {
		unregister((Object) pojo);
	}
	
	public void unregister(Object pojo) {
		bus.unregister(pojo);
		PeriodicCallbacks.unregister(pojo);
		synchronized (listeners) {
			listeners.remove(pojo);
		}
	}
	
	public void post(Object message) {
		bus.post(message);
	}
	
	public void handleMessage(Message msg) {
		bus.post(msg);
	}
	
	
	public void send(Message msg) {
		for (String peer : registry.peers()) {
			try {
				sendUdp(msg, peer);
			}
			catch (Exception e) {
				
			}
		}
	}

	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Message msg = ctx.getMessage();
		if (msg.mgid() == Announce.ID_STATIC)
			registry.setAnnounce((Announce) msg, (InetSocketAddress) ctx.getAddress());
		else if (msg.mgid() == EntityList.ID_STATIC)
			registry.setEntityList((EntityList) msg);

		handleMessage(msg);

		return ctx.getStopAction();
	}
	
	@Override
	public void post(Message msg) {
		bus.post(msg);
	}

	@Override
	public void send(Message msg, String destination) {
		try{
			sendUdp(msg, destination);		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Future<Boolean> deliver(Message msg, String destination) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			
			@Override
			public Boolean call() throws Exception {
				try{
					sendTcp(msg, destination);
					return true;
				}
				catch (Exception e) {
					return false;
				}				
			}
		};
		return executor.submit(callable);
	}

	@Override
	public int register(IMCActor actor, String name) {
		synchronized (listeners) {
			if (listeners.add(actor)) {
				bus.register(actor);
				PeriodicCallbacks.register(actor);
			}
		}
		return registry.registerLocalEntity(name);
	}

	@Override
	public ActorClock clock() {
		return clock;
	}

	@Override
	public IMCRegistry registry() {
		return registry;
	}
	
	
}