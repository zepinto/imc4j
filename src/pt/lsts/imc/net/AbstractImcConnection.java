package pt.lsts.imc.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.msg.Message;

public abstract class AbstractImcConnection extends BaseFilter {

	protected FilterChain filterChain;
	private HashSet<ImcPoller> pollers = new HashSet<>();

	public void addPoller(ImcPoller poller) {
		synchronized (pollers) {
			pollers.add(poller);
		}
	}

	public void removePoller(ImcPoller poller) {
		synchronized (pollers) {
			pollers.remove(poller);
		}
	}

	public AbstractImcConnection() {
		filterChain = IMCCodec.ImcFilter(this);
	}

	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Message msg = ctx.getMessage();
		if (msg.mgid() == Announce.ID_STATIC)
			IMCRegistry.setAnnounce((Announce) msg, (InetSocketAddress) ctx.getAddress());
		else if (msg.mgid() == EntityList.ID_STATIC)
			IMCRegistry.setEntityList((EntityList) msg);

		handleMessage(msg);

		synchronized (pollers) {
			for (ImcPoller p : pollers) {
				p.offer(msg);
			}
		}

		return ctx.getStopAction();
	}

	public abstract void handleMessage(Message msg);

	public abstract void sendMessage(Message msg);

	public Message poll(String type, long timeout) {
		ImcPoller poller = ImcPoller.ofType(this, type);
		return poller.poll(timeout);
	}

	public Message poll(String src, String type, long timeout) {

		long endTime = System.currentTimeMillis() + timeout;

		while (System.currentTimeMillis() < endTime) {
			if (IMCRegistry.resolveSystem(src) != null)
				return ImcPoller.ofTypeAndSource(this, type, src).poll(endTime - System.currentTimeMillis());

			try {
				Thread.sleep(100);
			} catch (Exception e) {
				break;
			}
		}

		return null;
	}

	public Message poll(long timeout) {
		ImcPoller poller = ImcPoller.any(this);
		return poller.poll(timeout);
	}

	public Message[] all(long timeout) {
		ImcPoller poller = ImcPoller.any(this);
		return poller.all(timeout);
	}
}
