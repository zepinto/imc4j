package pt.lsts.imcwan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class UdpListener extends Thread {

	private DatagramSocket socket = null;
	private boolean connected = false;
	private MessageProcessor processor;
	private int port;
	
	public UdpListener(int port, MessageProcessor processor) throws Exception {
		socket = new DatagramSocket(port);
		this.port = port;
		connected = true;
		this.processor = processor;
	}
	
	public static UdpListener firstAvailablePort(int fromPort, MessageProcessor processor) {
		UdpListener listener = null;
		while (listener == null) {
			try {
				listener = new UdpListener(fromPort, processor);
				return listener;
			}
			catch (Exception e) {
				fromPort++;
			}
		}
		return null;
	}
	
	public void sendMessage(byte[] data, SocketAddress destination) throws IOException {
		DatagramPacket packet = new DatagramPacket(data, data.length);
		packet.setSocketAddress(destination);
		socket.send(packet);
	}

	@Override
	public void run() {

		byte[] buffer = new byte[65535];

		while (connected) {
			try {
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				UnserializedMessage m = new UnserializedMessage(packet.getData());
				processor.processMessage((InetSocketAddress)packet.getSocketAddress(), m);

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

	@Override
	public void interrupt() {
		super.interrupt();
		connected = false;
	}

	public int getPort() {
		return port;
	}

	public static UdpListener discovery(MessageProcessor processor) throws Exception {
		for (int port = 30100; port < 30105; port++) {
			try {
				UdpListener server = new UdpListener(port, processor);
				System.out.println("Bound to port " + port);
				return server;
			} catch (Exception e) {
			}
		}
		throw new Exception("No ports available for discovery.");
	}
}
