package pt.lsts.imc4j.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EntityList;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.ActorClock;
import pt.lsts.imc4j.runtime.ActorContext;
import pt.lsts.imc4j.runtime.IMCActor;

public class UdpClient extends BaseFilter implements ActorContext {

	private int remoteId = 0xFFFF;
	private UDPNIOTransport udpTransport;
	private String host;
	private int port;
	
	public UdpClient(String host, int remoteport, int localport) throws Exception {
		this.host = host;
		this.port = remoteport;
		udpTransport = UDPNIOTransportBuilder.newInstance().build();
		udpTransport.setProcessor(IMCCodec.ImcFilter(this));
		udpTransport.configureBlocking(false);
		udpTransport.setReuseAddress(true);
		udpTransport.bind(localport);
		udpTransport.start();	
	}
	
	public void handleMessage(Message msg) {
		remoteId = msg.src;
	}
	
	
	@SuppressWarnings("unchecked")
	public void sendMessage(Message msg) {
		if (msg.src == 0xFFFF)
			msg.src = registry().getImcId();
		
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
		conn.sendMessage(new Abort());
		Thread.sleep(1000);
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
