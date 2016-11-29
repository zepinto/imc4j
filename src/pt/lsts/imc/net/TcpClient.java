package pt.lsts.imc.net;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;

import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.msg.EntityList.OP;
import pt.lsts.imc.msg.Message;

public class TcpClient extends AbstractImcConnection {

	private Connection<?> connection;
	private int remoteId = 0xFFFF;
	
	public TcpClient(String host, int port) throws Exception {
		TCPNIOTransport tcpTransport = TCPNIOTransportBuilder.newInstance().build();
		tcpTransport.setProcessor(filterChain);
		tcpTransport.configureBlocking(false);
		tcpTransport.start();
		connection = tcpTransport.connect(host, port).get();
	}
	
	@Override
	public void handleMessage(Message msg) {
		remoteId = msg.src;
	}
	
	
	public void sendMessage(Message msg) {
		msg.src = IMCRegistry.getImcId();
		msg.dst = remoteId;
		connection.write(msg);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(IMCRegistry.getImcId());
		TcpClient conn = new TcpClient("127.0.0.1", 9999);
		Thread.sleep(100);
		EntityList q = new EntityList();
		q.op = OP.OP_QUERY;
		conn.sendMessage(q);
		System.out.println(conn.poll("EntityList", 1000));
			
	}
}
