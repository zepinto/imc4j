package pt.lsts.imc.net;

import java.net.InetSocketAddress;
import java.util.Collection;

import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.def.SystemType;
import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.util.NetworkUtils;

public class IMCAnnouncer {
	private static final String multicastAddress = "224.0.75.69";
	private Announce announce = new Announce();

	public IMCAnnouncer(String systemName, int imcId, int localPort) {
		announce.src = imcId;
		announce.sys_name = systemName;
		announce.sys_type = SystemType.CCU;
		announce.services = "";

		Collection<String> netInt = NetworkUtils.getNetworkInterfaces();
		for (String itf : netInt) {
			announce.services += "imc+udp://" + itf + ":" + localPort + "/;";
			announce.services += "imc+tcp://" + itf + ":" + localPort + "/;";
		}
		if (announce.services.length() > 0)
			announce.services = announce.services.substring(0, announce.services.length() - 1);

	}

	@Periodic(10000)
	public void sendAnnounces() {
		for (int port = 30100; port < 30105; port++) {
			try {
				IMCNetwork.sendUdp(announce, new InetSocketAddress(multicastAddress, port));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
