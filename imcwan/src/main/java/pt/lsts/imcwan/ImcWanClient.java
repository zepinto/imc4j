package pt.lsts.imcwan;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.lsts.imc4j.msg.Announce;

public class ImcWanClient {

	private LinkedHashMap<Integer, InetSocketAddress> imcToLan = new LinkedHashMap<>();
	private LinkedHashMap<Integer, InetSocketAddress> imcToWan = new LinkedHashMap<>();
	private ArrayList<InetSocketAddress> localAddresses = new ArrayList<>();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private UdpListener wanConnection, lanConnection, lanDiscovery;
	private InetSocketAddress serverAddress;
	private boolean p2p = false;

	protected void lanDiscovery(InetSocketAddress addr, UnserializedMessage msg) {
		debug("Received message from DISCOVERY" + addr);
		// process incoming announce messages
		try {
			if (msg.mgid != Announce.ID_STATIC)
				return;
			// update list of known local peers
			synchronized (imcToLan) {
				imcToLan.put(msg.src, announceToAddress(addr, msg.get()));
			}

			// forward announces to remote server
			wanConnection.sendMessage(msg.serialize(), serverAddress);

			debug("Sent announce from " + msg.src + " running in " + addr + " to server.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void lanMessage(InetSocketAddress addr, UnserializedMessage msg) {
		debug("Received message from LAN" + addr);

		// requires UDP hole punching
		if (p2p) {
			// if we know about this remote destination, forward it
			InetSocketAddress wanAddress = imcToWan.get(msg.dst);
			if (wanAddress != null) {
				try {
					debug("Send " + msg.mgid + " from LAN" + addr + " to WAN" + wanAddress);
					wanConnection.sendMessage(msg.serialize(), wanAddress);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} 
		else {
			// forward all messages to server
			try {
				wanConnection.sendMessage(msg.serialize(), serverAddress);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void wanMessage(InetSocketAddress addr, UnserializedMessage msg) {
		debug("Received message from WAN" + addr);
		// When server announces remote peer, broadcast it to the local network
		if (addr.equals(serverAddress) && msg.mgid == Announce.ID_STATIC) {
			try {
				Announce ann = msg.get();
				InetSocketAddress remoteAddress = announceToAddress(addr, ann);
				synchronized (imcToWan) {
					imcToWan.put(msg.src, remoteAddress);
				}
				ann = wanToLan(ann);
				debug("Send Announce from WAN" + addr + " to broadcast");
				for (int port = 30100; port < 30105; port++) {
					if (port == lanDiscovery.getPort())
						continue;
					lanDiscovery.sendMessage(ann.serialize(), new InetSocketAddress("255.255.255.255", port));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// message must be coming from a remote peer
			try {
				// update remote peer table
				synchronized (imcToWan) {
					imcToWan.put(msg.src, addr);
				}
				InetSocketAddress uniqueDestination = null;
				synchronized(imcToLan) {
					uniqueDestination = imcToLan.get(msg.dst);
				}
				// forward message to final destination
				if (uniqueDestination != null) {
					debug("Send " + msg.mgid + " from WAN" + addr + " to LAN" + uniqueDestination);
					lanConnection.sendMessage(msg.serialize(), uniqueDestination);
				}
				// message is broadcast (as typically from DUNE
				else if (msg.dst == 0xFFFF && isVehicle(msg.src)) {
					LinkedHashMap<Integer, InetSocketAddress> localPeers = new LinkedHashMap<>();
					synchronized (imcToLan) {
						localPeers.putAll(imcToLan);
					}
					localPeers.entrySet().stream().forEach(peer -> {
						if (isConsole(peer.getKey()))
							try {
								lanConnection.sendMessage(msg.serialize(), peer.getValue());
							} catch (IOException e) {
								e.printStackTrace();
							}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private InetSocketAddress announceToAddress(InetSocketAddress addr, Announce msg) {
		String[] services = msg.services.split(";");

		// find all services for imc+udp
		Pattern udpService = Pattern.compile("imc\\+udp://(.+):(.+)/(.*)");
		HashSet<InetSocketAddress> addrs = new HashSet<>();
		for (String service : services) {
			if (service.startsWith("imc+udp://")) {
				Matcher matcher = udpService.matcher(service);
				if (matcher.matches()) {
					addrs.add(new InetSocketAddress(matcher.group(1), Integer.valueOf(matcher.group(2))));
				}
			}
		}
		// if there is no imc+udp service, bad luck
		if (addrs.isEmpty())
			return null;

		// if there are multiple imc+udp services, try to find one matching the received
		// address
		if (addrs.size() > 1) {
			for (InetSocketAddress a : addrs) {
				if (a.getAddress().equals(addr.getAddress()))
					return a;
			}
		}
		// otherwise just return the first one
		return addrs.iterator().next();
	}

	private Announce wanToLan(Announce msg) {
		String[] services = msg.services.split(";");
		ArrayList<String> updatedServices = new ArrayList<>();

		// drop not forwardable services and update udp services to local addresses
		for (String service : services) {
			if (service.startsWith("ftp://"))
				continue;
			if (service.startsWith("http://"))
				continue;
			if (service.startsWith("imc+tcp://"))
				continue;
			if (service.startsWith("imc+udp://")) {
				for (InetSocketAddress addr : localAddresses) {
					service = "imc+udp://" + addr.getAddress().getHostAddress() + ":" + addr.getPort()
							+ service.substring(service.indexOf("/", 11));
					updatedServices.add(service);
				}
			} else
				updatedServices.add(service);
		}

		msg.services = String.join(";", updatedServices);
		msg.timestamp = System.currentTimeMillis() / 1000.0;

		return msg;
	}

	private void findLocalAddresses() {
		try {
			Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces();
			while (itfs.hasMoreElements()) {
				NetworkInterface n = itfs.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = ee.nextElement();
					if (!(i instanceof Inet4Address) || i.isLoopbackAddress())
						continue;
					localAddresses.add(new InetSocketAddress(i.getHostAddress(), lanConnection.getPort()));
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public ImcWanClient(String serverHostname, int serverPort) throws Exception {
		serverAddress = new InetSocketAddress(serverHostname, serverPort);

		debug("Creating WAN with server in " + serverAddress.getHostString() + ":" + serverAddress.getPort());

		wanConnection = UdpListener.firstAvailablePort(35001, this::wanMessage);
		wanConnection.start();

		debug("WAN connection listening on port " + wanConnection.getPort());

		lanConnection = UdpListener.firstAvailablePort(35002, this::lanMessage);
		lanConnection.start();

		findLocalAddresses();

		debug("LAN connection listening on port " + lanConnection.getPort());

		lanDiscovery = UdpListener.discovery(this::lanDiscovery);
		lanDiscovery.start();
	}

	private boolean isConsole(int id) {
		return ((id >> (16 - 3)) ^ 0b010) == 0;
	}

	private boolean isVehicle(int id) {
		return ((id >> (16 - 2)) ^ 0b00) == 0;
	}

	public void debug(String string) {
		System.out.println("[WanClient " + sdf.format(new Date()) + "] " + string);
	}

	public static void main(String[] args) throws Exception {
		String host = "ripples.lsts.pt";
		int port = 35000;

		if (args.length == 2) {
			try {
				port = Integer.parseInt(args[1]);
				host = args[0];
			} catch (Exception e) {
				System.out.println("Usage: ./imcproxy <host> <port>");
				return;
			}
		}

		new ImcWanClient(host, port);
	}
}
