package pt.lsts.imc4j.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkUtils {

	public static List<String> getNetworkInterfaces() {
		List<String> itfs = getNetworkInterfaces(false);
		if (itfs.isEmpty())
			itfs = getNetworkInterfaces(true);
		return itfs;
	}
	
	public static List<String> getNetworkInterfaces(boolean includeLoopback) {
		ArrayList<String> itfs = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				NetworkInterface ni = nis.nextElement();
				try {
					if (ni.isLoopback() && !includeLoopback)
						continue;
				} catch (Exception e) {
					continue;
				}

				Enumeration<InetAddress> adrs = ni.getInetAddresses();
				while (adrs.hasMoreElements()) {
					InetAddress addr = adrs.nextElement();
					if (addr instanceof Inet4Address)
						itfs.add(addr.getHostAddress());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return itfs;
	}
}
