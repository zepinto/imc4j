package pt.lsts.imc4j.net;

import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.Heartbeat;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;
import pt.lsts.imc4j.util.SerializationUtils;

import javax.annotation.processing.Completion;
import javax.xml.crypto.Data;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImcPeer {

    private Announce lastAnnounce = null;
    private Heartbeat lastHeartbeat = null;
    private String services = "";

    private long lastHeartbeatMillis = 0;
    private static final long aliveThresholdMillis = 30_000;
    private int remoteId;
    private InetSocketAddress tcpAddress = null, udpAddress = null;
    private Pattern pProto = Pattern
            .compile("imc\\+(\\w+)://(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+):(\\d+)/");

    public ImcPeer(int imcId) {
        this.remoteId = imcId;
    }

    public int getRemoteId() {
        return remoteId;
    }

    public void setMessage(Message m, SocketAddress source) {
        switch (m.mgid()) {
            case Heartbeat.ID_STATIC:
                lastHeartbeatMillis = System.currentTimeMillis();
                lastHeartbeat = (Heartbeat) m;
                break;
            case Announce.ID_STATIC:
                lastAnnounce = (Announce) m;
                if (!services.equals(lastAnnounce.services)) {
                    if (parseServices(lastAnnounce.services, source))
                        services = lastAnnounce.services;
                }
                break;
            default:
                break;
        }
    }

    private boolean parseServices(String services, SocketAddress sourceAddress) {
        String[] servs = lastAnnounce.services.split(";");
        LinkedHashSet<InetSocketAddress> udpAddresses = new LinkedHashSet<InetSocketAddress>();
        LinkedHashSet<InetSocketAddress> tcpAddresses = new LinkedHashSet<InetSocketAddress>();

        System.out.println("Received announce from "+sourceAddress+": "+services);
        for (String serv : servs) {
            if (pProto.matcher(serv).matches()) {
                Matcher mProto = pProto.matcher(serv);
                String proto = mProto.group(1);
                String hostname = mProto.group(2) + "." + mProto.group(3) + "."
                        + mProto.group(4) + "." + mProto.group(5);
                int port = Integer.valueOf(mProto.group(6));

                if (proto.equals("udp"))
                    udpAddresses.add(new InetSocketAddress(hostname, port));
                else if (proto.equals("tcp"))
                    tcpAddresses.add(new InetSocketAddress(hostname, port));
            }
        }

        System.out.println(udpAddresses + "\n" + tcpAddresses);
        return true;
    }

    public Future<Void> deliver(Message m) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (tcpAddress == null)
                    throw new Exception("Peer does not have a TCP endpoint");
                SocketChannel tmpChannel = SocketChannel.open(tcpAddress);
                Message clone = SerializationUtils.clone(m);
                clone.dst = remoteId;
                ByteBuffer data = ByteBuffer.wrap(clone.serialize());

                while (data.hasRemaining())
                    tmpChannel.write(data);

                tmpChannel.close();
            }
            catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    public void dispatch(Message m) throws Exception {
        if (udpAddress == null)
            throw new Exception("Peer does not have a UDP endpoint");
        Message clone = SerializationUtils.clone(m);
        m.dst = remoteId;

        DatagramChannel tmpChannel = DatagramChannel.open();
        tmpChannel.send(ByteBuffer.wrap(clone.serialize()), udpAddress);
        tmpChannel.close();
    }

    boolean isAlive() {
        return (System.currentTimeMillis() - lastHeartbeatMillis) < aliveThresholdMillis;
    }

    long getLastHeartBeatMillis() {
        return lastHeartbeatMillis;
    }

    public static void main(String[] args) throws Exception {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3_000);
            }
            catch (Exception e) {
                throw new CompletionException(new Exception("tsdfasd"));
            }
            throw new CompletionException(new Exception("tsdf"));

        });
        try {
            System.out.println(future.get(5_000, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}