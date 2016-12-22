package pt.lsts.imc4j.net;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;

import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;

public class TcpClient extends AbstractActorContext {

	private Connection<?> connection;
	private int remoteId = 0xFFFF;
	private TCPNIOTransport tcpTransport;
	private String host;
	private int port;
	
	public TcpClient(String host, int port) throws Exception {
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
	}

	@Override
	public void onStop() throws Exception {
		tcpTransport.shutdownNow();
	}

	@Override
	public void send(Message msg) {
		fillIn(msg);
		msg.dst = remoteId;
		connection.write(msg);
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
		TcpClient conn = new TcpClient("127.0.0.1", 6002);
		conn.start();
		while(true) {
			System.out.println(conn.query(EstimatedState.class).now());
			Thread.sleep(5000);
			conn.send(new Abort());
		}		
	}
}
