package pt.lsts.imcwan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.util.PeriodicCallbacks;

public class ImcWanServer {

	private UdpListener udp;
	private LinkedHashMap<InetSocketAddress, Announce> announces = new LinkedHashMap<>();
	private LinkedHashMap<Integer, byte[]> lastMessages = new LinkedHashMap<Integer, byte[]>(101, .75F, true) {
	 	private static final long serialVersionUID = 7281860382373828464L;
	 	@Override
	 	public boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
	        return size() > 100;
	    }
	};
	
	public ImcWanServer(int port) throws Exception {
		debug("Listening on port " + port);
		udp = new UdpListener(port, (addr, msg) -> {
			if (msg.mgid == Announce.ID_STATIC) {
				try {
					synchronized (announces) {
						debug("received announce from " + addr);
						Announce updated = updateAnnounce(msg.get(), addr);
						announces.put(addr, updated);
						forward(addr, updated.serialize());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else
				forward(addr, msg.serialize());
		});
		udp.start();
		PeriodicCallbacks.register(this);
	}

	public void forward(InetSocketAddress source, byte data[]) {
		
		// remove duplicates
		int key = (data[data.length-2] << 16 | data[data.length-1]);
		synchronized (lastMessages) {
			byte[] before = lastMessages.put(key, data);
			if (before != null) {
				debug("Not forwarding duplicate message");
				return;
			}
		}
		
		synchronized (announces) {
			for (Entry<InetSocketAddress, Announce> entry : announces.entrySet()) {
				if (!entry.getKey().equals(source)) {

					try {
						// send the remote announce to this peer
						debug("Forward message from "+source+" to "+entry.getKey());
						udp.sendMessage(data, entry.getKey());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

	@Periodic(60_000)
	public void clearOldAnnounces() {

		debug("clear old announces");

		double minTimeStamp = System.currentTimeMillis() / 1000.0 - 60;
		synchronized (announces) {
			Iterator<Entry<InetSocketAddress, Announce>> it = announces.entrySet().iterator();
			while (it.hasNext()) {
				Entry<InetSocketAddress, Announce> e = it.next();
				if (e.getValue().timestamp < minTimeStamp)
					it.remove();
			}
		}
	}

	// Update the announce message by replacing all of its udp addresses and
	// updating the timestamp
	public Announce updateAnnounce(Announce original, InetSocketAddress newAddress) {
		String[] services = original.services.split(";");
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
				service = "imc+udp://" + newAddress.getAddress().getHostAddress() + ":" + newAddress.getPort()
				+ service.substring(service.indexOf("/", 11));
				updatedServices.add(service);
			} else
				updatedServices.add(service);
		}

		original.services = String.join(";", updatedServices);
		original.timestamp = System.currentTimeMillis() / 1000.0;
		return original;
	}
	


	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void debug(String string) {
		System.out.println("[WanServer " + sdf.format(new Date()) + "] " + string);
	}

	public static void main(String[] args) throws Exception {
		new ImcWanServer(35000);
	}

}
