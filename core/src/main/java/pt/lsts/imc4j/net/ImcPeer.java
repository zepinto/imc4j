package pt.lsts.imc4j.net;

import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.Heartbeat;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.ImcConsumable;

import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents an IMC peer in the network from which messages can arrive and can be sent to
 */
public class ImcPeer implements ImcConsumable {

    private Announce lastAnnounce = null;
    private Heartbeat lastHeartbeat = null;
    private String services = "";
    private long lastMessageMillis = 0;
    private static final long aliveThresholdMillis = 30_000;
    private final Pattern pProto = Pattern
            .compile("imc\\+(.*)://(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+):(\\d+)/");

    private int remoteId, localId;
    private InetSocketAddress tcpAddress = null;
    private InetSocketAddress udpAddress = null;
    private boolean active = false;

    /**
     * Constructor. Create a Peer given its first announce and local id.
     * @param announce The announce received from peer.
     * @param localId The IMC ID of the local node
     */
    public ImcPeer(Announce announce, int localId) {
        this.lastAnnounce = announce;
        this.remoteId = announce.src;
        this.localId = localId;
        setMessage(announce);
    }

    /**
     * @return the remote IMC ID
     */
    public int getId() {
        return lastAnnounce.src;
    }

    /**
     * @return the name of the remote peer as stated in its Announce.
     */
    public String getName() {
        return lastAnnounce.sys_name;
    }

    /**
     * @return the type of the remote peer as stated in its Announce.
     */
    public SystemType getType() {
        return lastAnnounce.sys_type;
    }

    /**
     * Checks if new information is being received from remote peer
     * @return <code>true</code> if last received message is still "fresh".
     */
    boolean isAlive() {
        return (System.currentTimeMillis() - lastMessageMillis) < aliveThresholdMillis;
    }

    /**
     * (De)activate transmission to this peer.
     * @param active If set to <code>true</code>, network will send heartbeat to this peer.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check if this peer is active.
     * @return <code>true</code> if the peer is marked active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Send a message unreliably to this peer
     * @param m The message to send
     * @throws Exception If the peer doesn't have an UDP endpoint
     */
    public void send(Message m) throws Exception {
        m.src = localId;
        m.dst = remoteId;
        if (udpAddress != null)
            ImcTransport.sendViaUdp(m, udpAddress.getHostName(), udpAddress.getPort());
        else
            throw new Exception("Peer does not have a UDP endpoint");
    }

    /**
     * Send a message reliably to this peer
     * @param m The message to send
     * @throws Exception If the peer doesn't have a TCP endpoint
     */
    public Future<Void> deliver(Message m) throws Exception {
        m.src = localId;
        m.dst = remoteId;
        if (getTcpAddress() != null)
            return ImcTransport.sendViaTcp(m, tcpAddress.getHostName(), tcpAddress.getPort());
        else
            throw new Exception("Peer does not have a TCP endpoint");
    }

    /**
     * Check whether this peer can be contacted using TCP
     * @return <code>true</code> if a TCP endpoint exists for this peer
     */
    public boolean hasTcp() {
        return tcpAddress != null;
    }

    /**
     * Check whether this peer can be contacted using UDP
     * @return <code>true</code> if a UDP endpoint exists for this peer
     */
    public boolean hasUdp() {
        return udpAddress != null;
    }

    protected InetSocketAddress getTcpAddress() {
        return tcpAddress;
    }

    protected InetSocketAddress getUdpAddress() {
        return udpAddress;
    }

    protected void setMessage(Message m) {
        lastMessageMillis = System.currentTimeMillis();
        switch (m.mgid()) {
            case Heartbeat.ID_STATIC:
                lastHeartbeat = (Heartbeat) m;
                break;
            case Announce.ID_STATIC:
                lastAnnounce = (Announce) m;
                if (!services.equals(lastAnnounce.services)) {
                    parseServices(lastAnnounce.services);
                    services = lastAnnounce.services;
                }
                break;
            default:
                break;
        }

        publish(m);
    }

    private void parseServices(String services) {
        String[] servs = lastAnnounce.services.split(";");
        LinkedHashSet<InetSocketAddress> udpAddresses = new LinkedHashSet<>();
        LinkedHashSet<InetSocketAddress> tcpAddresses = new LinkedHashSet<>();

        for (String serv : servs) {
            Matcher mProto = pProto.matcher(serv);
            if (mProto.matches()) {
                String proto = mProto.group(1);
                String hostname = mProto.group(2) + "." + mProto.group(3) + "."
                        + mProto.group(4) + "." + mProto.group(5);
                int port = Integer.parseInt(mProto.group(6));

                if (proto.equals("udp"))
                    udpAddresses.add(new InetSocketAddress(hostname, port));
                else if (proto.equals("tcp"))
                    tcpAddresses.add(new InetSocketAddress(hostname, port));
            }
        }
        if (udpAddresses.iterator().hasNext())
            udpAddress = udpAddresses.iterator().next();
        if (tcpAddresses.iterator().hasNext())
            tcpAddress = tcpAddresses.iterator().next();
    }
}