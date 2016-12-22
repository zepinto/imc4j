package pt.lsts.imc4j.runtime;

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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.nio.transport.UDPNIOConnection;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import pt.lsts.imc4j.actors.EntityListRequester;
import pt.lsts.imc4j.actors.IMCAnnouncer;
import pt.lsts.imc4j.actors.IMCBeater;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.actors.AbstractActorContext;
import pt.lsts.imc4j.runtime.actors.ActorContext;
import pt.lsts.imc4j.util.NetworkUtils;

public class IMCRuntime extends AbstractActorContext implements ActorContext {

	private Bus bus = new Bus(ThreadEnforcer.ANY);
	private FilterChain filterChain;
	private UDPNIOTransport udpTransport = null;
	private TCPNIOTransport tcpTransport = null;
	private UDPNIOConnection udpMulticast = null;

	public IMCRuntime() {
		filterChain = IMCCodec.ImcFilter(this);
		new IMCBeater(this);
		new IMCAnnouncer(this);
		new EntityListRequester(this);
	}

	public void onStart() throws Exception {
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
			registry().addService("imc+udp://" + itf + ":" + udpPort + "/");
			registry().addService("imc+tcp://" + itf + ":" + udpPort + "/");
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

	public void onStop() throws Exception {
		if (udpMulticast != null)
			udpMulticast.closeSilently();
		if (udpTransport != null)
			udpTransport.shutdownNow();
		if (tcpTransport != null)
			tcpTransport.shutdownNow();
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
		Integer imcid = registry().resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = registry().udpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over UDP");

		msg.dst = imcid;
		fillIn(msg);
		sendUdp(msg, addr);
		bus.post(msg);
	}

	public void sendTcp(Message msg, String dst) throws Exception {
		Integer imcid = registry().resolveSystem(dst);
		if (imcid == null)
			throw new Exception("Peer is not available");

		InetSocketAddress addr = registry().tcpAddress(dst);
		if (addr == null)
			throw new Exception("Peer is not available over TCP");

		msg.dst = imcid;
		fillIn(msg);
		sendTcp(msg, addr);
		bus.post(msg);
	}

	@Override
	public void send(Message msg, String destination) throws Exception {
		sendUdp(msg, destination);
	}

	@Override
	public Future<Boolean> deliver(Message msg, String destination) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				try {
					sendTcp(msg, destination);
					return true;
				} catch (Exception e) {
					return false;
				}
			}
		};
		return executor.submit(callable);
	}
}