package pt.lsts.imcwan;

import java.net.InetSocketAddress;

public interface MessageProcessor {
	public void processMessage(InetSocketAddress source, UnserializedMessage message);
}
