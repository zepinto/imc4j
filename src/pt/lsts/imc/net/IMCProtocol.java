package pt.lsts.imc.net;

import java.io.IOException;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.UDPNIOTransport;
import org.glassfish.grizzly.nio.transport.UDPNIOTransportBuilder;

import pt.lsts.imc.msg.Message;

public class IMCProtocol extends BaseFilter {
	
	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Message msg = ctx.getMessage();
		System.out.println(msg.mgid());
		return ctx.getStopAction();
	}

	public static void main(String[] args) throws IOException {
		UDPNIOTransport udpTransport = UDPNIOTransportBuilder.newInstance().build();
		
		FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add(new TransportFilter());
        filterChainBuilder.add(new IMCCodec());
        filterChainBuilder.add(new IMCProtocol());
		udpTransport.setProcessor(filterChainBuilder.build());
		
		udpTransport.setReuseAddress(true);
		udpTransport.configureBlocking(false);
		udpTransport.bind(6001);
		udpTransport.bind(6003);
		udpTransport.start();
		
		System.out.println("Press any key to stop the server...");
        System.in.read();
        udpTransport.shutdownNow();
	}
}
