package pt.lsts.imc4j.net;

import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;

public class UdpClient extends AbstractActorContext {

	private int remoteId = 0xFFFF;
	private UDPNIOTransport udpTransport;
	private String host;
	private int port;
	private int localport;
	
	public UdpClient(String host, int remoteport, int localport) throws Exception {
		this.host = host;
		this.port = remoteport;
		this.localport = localport;		
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		remoteId = msg.src;
	}	
	
	@Override
	public void onStart() throws Exception {
		udpTransport = UDPNIOTransportBuilder.newInstance().build();
		udpTransport.setProcessor(IMCCodec.ImcFilter(this));
		udpTransport.configureBlocking(false);
		udpTransport.setReuseAddress(true);
		udpTransport.bind(this.localport);
		udpTransport.start();	
	}

	@Override
	public void onStop() throws Exception {
		udpTransport.shutdownNow();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void send(Message msg) {
		fillIn(msg);
		msg.dst = remoteId;
		try {
			udpTransport.connect(host, port).get().write(msg);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void send(Message msg, String destination) throws Exception {
		fillIn(msg);
		if (msg.dst == 0xFFFF)
			msg.dst = remoteId;
		
		udpTransport.connect(host, port).get().write(msg);		
	}
	
	public static void main(String[] args) throws Exception {
		UdpClient conn = new UdpClient("127.0.0.1", 6002, 6001);
		conn.start();
		while(true) {
			System.out.println(conn.query(EstimatedState.class).now());
			Thread.sleep(5000);
			conn.send(new Abort());
		}		
	}
}
