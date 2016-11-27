package pt.lsts.imc.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.Message;

public abstract class AbstractImcConnection  extends BaseFilter {
	
	protected FilterChain filterChain;
	
	public AbstractImcConnection() {
		filterChain = IMCCodec.ImcFilter(this);
	}
	
	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Message msg = ctx.getMessage();
		if (msg instanceof Announce)
			IMCRegistry.setAnnounce((Announce)msg, (InetSocketAddress) ctx.getAddress());
		
		handleMessage(msg);
		return ctx.getStopAction();
	}
	
	public abstract void handleMessage(Message msg);
	
	public void sendMessage(Message msg) {
		msg.src = IMCRegistry.getImcId();
	}
}
