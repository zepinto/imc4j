package pt.lsts.imc4j.runtime;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;

import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.actors.AbstractActorContext;

public class TcpRuntime extends AbstractActorContext {

	private Connection<?> connection;
	private int remoteId = 0xFFFF;
	private TCPNIOTransport tcpTransport;
	private String host;
	private int port;
	
	public TcpRuntime(String host, int port) throws Exception {
		this.host = host;
		this.port = port;
		tcpTransport = TCPNIOTransportBuilder.newInstance().build();
		tcpTransport.setProcessor(IMCCodec.ImcFilter(this));
		tcpTransport.configureBlocking(false);		
	}

	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		remoteId = msg.src;
	}

	@Override
	public void onStart() throws Exception {
		tcpTransport.start();
		connection = tcpTransport.connect(host, port).get();
		send(registry().buildAnnounce());
	}

	@Override
	public void onStop() throws Exception {
		tcpTransport.shutdownNow();
	}

	@Override
	public int send(Message msg) throws Exception {
		fillIn(msg);
		msg.dst = remoteId;
		connection.write(msg);
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
	public void send(Message msg, String destination) throws Exception {
		fillIn(msg);
		Integer imcid = registry().resolveSystem(destination);

		if (imcid == null)
			throw new Exception(destination+ " is unknown.");

		msg.src = registry().getImcId();
		msg.dst = imcid;
		connection.write(msg);
	}
	
	public static void main(String[] args) throws Exception {
		TcpRuntime context = new TcpRuntime("127.0.0.1", 6002);
		context.start();
		while(true) {
			context.clock().sleep(5000);
			System.out.println(context.query(EstimatedState.class).now());
			System.out.println(context.peers());
			context.send(new Abort());
		}		
	}
}
