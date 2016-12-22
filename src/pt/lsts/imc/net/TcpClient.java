package pt.lsts.imc.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;

import pt.lsts.imc.actors.ActorClock;
import pt.lsts.imc.actors.ActorContext;
import pt.lsts.imc.actors.IMCActor;
import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.msg.Message;

public class TcpClient extends BaseFilter implements ActorContext {

	private Connection<?> connection;
	private int remoteId = 0xFFFF;
	
	public TcpClient(String host, int port) throws Exception {
		TCPNIOTransport tcpTransport = TCPNIOTransportBuilder.newInstance().build();
		tcpTransport.setProcessor(IMCCodec.ImcFilter(this));
		tcpTransport.configureBlocking(false);
		tcpTransport.start();
		connection = tcpTransport.connect(host, port).get();
	}
	
	public void handleMessage(Message msg) {
		remoteId = msg.src;
	}
	
	
	public void sendMessage(Message msg) {
		msg.src = registry().getImcId();
		msg.dst = remoteId;
		connection.write(msg);
	}
	
	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Message msg = ctx.getMessage();
		if (msg.mgid() == Announce.ID_STATIC)
			registry().setAnnounce((Announce) msg, (InetSocketAddress) ctx.getAddress());
		else if (msg.mgid() == EntityList.ID_STATIC)
			registry().setEntityList((EntityList) msg);

		handleMessage(msg);

		return ctx.getStopAction();
	}
	
	@Override
	public void post(Message msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(Message msg, String destination) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Future<Boolean> deliver(Message msg, String destination) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int register(IMCActor actor, String name) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ActorClock clock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMCRegistry registry() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unregister(IMCActor actor) {
		// TODO Auto-generated method stub
		
	}

}
