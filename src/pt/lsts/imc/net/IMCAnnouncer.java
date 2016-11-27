package pt.lsts.imc.net;

import java.net.InetSocketAddress;

import pt.lsts.imc.annotations.Periodic;

/**
 * This module will send announces to multicast address every 10 seconds (as
 * required by the IMC protocol)
 * 
 * @author zp
 *
 */
public class IMCAnnouncer {
	private static final String multicastAddress = "224.0.75.69";

	@Periodic(10000)
	public void sendAnnounces() {
		for (int port = 30100; port < 30105; port++) {
			try {
				IMCNetwork.sendUdp(IMCRegistry.buildAnnounce(), new InetSocketAddress(multicastAddress, port));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
