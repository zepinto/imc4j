package pt.lsts.imc.net;

import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import pt.lsts.imc.msg.Abort;
import pt.lsts.imc.msg.Message;

public class UdpClient extends AbstractImcConnection {

	private int remoteId = 0xFFFF;
	private UDPNIOTransport udpTransport;
	private String host;
	private int port;
	
	public UdpClient(String host, int remoteport, int localport) throws Exception {
		this.host = host;
		this.port = remoteport;
		udpTransport = UDPNIOTransportBuilder.newInstance().build();
		udpTransport.setProcessor(filterChain);
		udpTransport.configureBlocking(false);
		udpTransport.setReuseAddress(true);
		udpTransport.bind(localport);
		udpTransport.start();	
	}
	
	@Override
	public void handleMessage(Message msg) {
		remoteId = msg.src;
	}
	
	
	@SuppressWarnings("unchecked")
	public void send(Message msg) {
		if (msg.src == 0xFFFF)
			msg.src = IMCRegistry.getImcId();
		
		if (msg.dst == 0xFFFF)
			msg.dst = remoteId;
		
		try {
			udpTransport.connect(host, port).get().write(msg);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) throws Exception {
		UdpClient conn = new UdpClient("127.0.0.1", 6002, 6001);
		Thread.sleep(1000);
		conn.send(new Abort());
		Thread.sleep(1000);
	}
}
