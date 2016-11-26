package pt.lsts.imc.net;

import java.net.InetSocketAddress;
import java.util.Collection;

import pt.lsts.imc.annotations.Periodic;
import pt.lsts.imc.def.SystemType;
import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.util.NetworkUtils;

/**
 * This module will send announces to multicast address every 10 seconds (as
 * required by the IMC protocol)
 * 
 * @author zp
 *
 */
public class IMCAnnouncer {
	private static final String multicastAddress = "224.0.75.69";
	private Announce announce = new Announce();

	/**
	 * Create an announcer module
	 * @param systemName The local name to announce
	 * @param imcId The local imc id to announce
	 * @param localPort The port where UDP and TCP server is listening (<= 0 if not listening)
	 */
	public IMCAnnouncer(String systemName, int imcId, int localPort) {
		announce.src = imcId;
		announce.sys_name = systemName;
		announce.sys_type = SystemType.CCU;
		announce.services = "";

		if (localPort > 0) {
			Collection<String> netInt = NetworkUtils.getNetworkInterfaces();
			for (String itf : netInt) {
				announce.services += "imc+udp://" + itf + ":" + localPort + "/;";
				announce.services += "imc+tcp://" + itf + ":" + localPort + "/;";
			}
		}
		
		if (announce.services.length() > 0)
			announce.services = announce.services.substring(0, announce.services.length() - 1);
	}
	
	/**
	 * Change the name to use when announcing this system
	 * @param sys_name The name to use
	 */
	public void setSysName(String sys_name) {
		this.announce.sys_name = sys_name;
	}
	
	/**
	 * Change the IMC id of this system
	 * @param id The id to announce
	 */
	public void setImcId(int id) {
		this.announce.src = id;
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
