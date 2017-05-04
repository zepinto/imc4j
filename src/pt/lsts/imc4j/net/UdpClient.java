package pt.lsts.imc4j.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashSet;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.ImcConsumer;
import pt.lsts.imc4j.util.PeriodicCallbacks;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * @author zp
 *
 */
public class UdpClient extends Thread {

	private DatagramSocket socket = null;
	private HashSet<ImcConsumer> consumers = new HashSet<ImcConsumer>();
	private boolean connected = false;
	public int remoteSrc = 0;
	public int localSrc = 0x555;

	public int remotePort;
	public String remoteAddr;

	public void bind(int localPort) throws Exception {
		if (socket != null)
			socket.close();
		socket = new DatagramSocket(localPort);
		connected = true;
		start();

	}

	public void connect(String host, int remotePort) throws Exception {
		this.remoteAddr = host;
		this.remotePort = remotePort;
	}

	@Override
	public void interrupt() {
		super.interrupt();
		connected = false;
	}

	@Override
	public void run() {
		byte[] buffer = new byte[65535];
		try {
			if (socket == null)
				socket = new DatagramSocket();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		while (connected) {
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				Message m = SerializationUtils.deserializeMessage(buffer);
				if (m != null)
					dispatch(m);
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		try {
			socket.close();
			socket = null;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void send(Message m) throws IOException {
		m.dst = remoteSrc;
		m.src = localSrc;
		m.timestamp = System.currentTimeMillis() / 1000.0;

		if (socket == null)
			socket = new DatagramSocket();

		try {
			byte[] data = m.serialize();
			DatagramPacket packet = new DatagramPacket(data, data.length,
					new InetSocketAddress(remoteAddr, remotePort));
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Sent message to " + remoteAddr + ":" + remotePort);
	}

	private void dispatch(Message m) {
		if (remoteSrc == 0)
			remoteSrc = m.src;

		for (ImcConsumer consumer : consumers)
			consumer.onMessage(m);
	}

	public synchronized void register(Object pojo) {
		PeriodicCallbacks.register(pojo);
		consumers.add(ImcConsumer.create(pojo));
	}

	public synchronized void unregister(Object pojo) {
		PeriodicCallbacks.unregister(pojo);
		ImcConsumer c = null;
		for (ImcConsumer consumer : consumers) {
			if (consumer.getPojo() == pojo) {
				c = consumer;
				break;
			}
		}

		if (c != null)
			consumers.remove(c);
	}
}
