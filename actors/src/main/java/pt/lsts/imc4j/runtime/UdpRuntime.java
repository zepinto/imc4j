package pt.lsts.imc4j.runtime;

import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.actors.AbstractActorContext;

public class UdpRuntime extends AbstractActorContext {

	private int remoteId = 0xFFFF;
	private UDPNIOTransport udpTransport;
	private String host;
	private int port;
	private int localport;
	
	public UdpRuntime(String host, int remoteport, int localport) throws Exception {
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
		send(registry().buildAnnounce());
	}

	@Override
	public void onStop() throws Exception {
		udpTransport.shutdownNow();
	}

	@Override
	@SuppressWarnings("unchecked")
	public int send(Message msg) throws Exception {
		fillIn(msg);
		msg.dst = remoteId;
		udpTransport.connect(host, port).get().write(msg);
		return 1;
	}
	
	@Override
	public void reply(Message request, Message reply) throws Exception {
		if (request.src == remoteId)
			send(reply);
		else
			super.reply(request, reply);
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
		UdpRuntime conn = new UdpRuntime("127.0.0.1", 6002, 6001);
		conn.start();
		while(true) {
			System.out.println(conn.query(EstimatedState.class).now());
			Thread.sleep(5000);
			conn.send(new Abort());
		}		
	}
}
