package pt.lsts.imc4j.net;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Heartbeat;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.AbstractMessage;
import pt.lsts.imc4j.util.NetworkUtils;
import pt.lsts.imc4j.util.PeriodicCallbacks;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class implements a connection to an IMC Network
 */
public class ImcNetwork implements ImcTransport.MsgHandler {
    private final ImcTransport imcTransport;
    private final Announce localNode;
    private final ConcurrentHashMap<Integer, ImcPeer> peers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Message>, ArrayList<Consumer<? extends Message>>> consumers
            = new ConcurrentHashMap<>();
    Predicate<ImcPeer> connectionPolicy = p -> true;

    /**
     * Constructor creates a local IMC node (not connected initially).
     * @param sysName The name of the local IMC node
     * @param imcId The IMC ID for the local IMC node
     * @param sysType The type of system of the local IMC node
     * @see {@link #bindTcp(int)}
     * @see {@link #bindUdp(int)}
     * @see {@link #bindDiscovery()}
     */
    private ImcNetwork(String sysName, int imcId, SystemType sysType) {
        localNode = new Announce();
        localNode.sys_name = sysName;
        localNode.sys_type = sysType;
        localNode.src = imcId;
        imcTransport = new ImcTransport();
        imcTransport.setDaemon(true);
        imcTransport.addHandler(this);
        PeriodicCallbacks.register(this);
    }

    /**
     * Set the location of this IMC node
     * @param latDegrees latitude, in degrees
     * @param lonDegrees longitude, in degrees
     * @param height Height above MSL
     */
    public void setLocation(double latDegrees, double lonDegrees, float height) {
        localNode.lat = Math.toRadians(latDegrees);
        localNode.lon = Math.toRadians(lonDegrees);
        localNode.height = height;
    }

    /**
     * @param id The IMC Identifier of the node controlling this one
     */
    public void setOwner(int id) {
        localNode.owner = id;
    }

    /**
     * Add a message consumer
     * @param msg The class of the messages to consume
     * @param consumer A method reference for the consumer of specified message type
     */
    public <M extends Message> void bind(Class<M> msg, Consumer<M> consumer) {
        synchronized (consumers) {
            consumers.putIfAbsent(msg, new ArrayList<>());
            consumers.get(msg).add(consumer);
        }
    }

    /**
     * Given a recently visible peer, this predicate decides whether to actively connect to it or not
     * @param policy A predicate that decides whether to connect to given peer
     */
    public void setConnectionPolicy(Predicate<ImcPeer> policy) {
        this.connectionPolicy = policy;
    }

    @Override
    public void handleMessage(Message msg, SocketAddress remoteAddress) {
        ImcPeer peer = peers.get(msg.src);
        if (peer == null) {
            if (msg.mgid() == Announce.ID_STATIC) {
                peer = new ImcPeer((Announce) msg);
                if (connectionPolicy.test(peer))
                    peer.setActive(true);
            }
            else
                return;
        }
        peer.setMessage(msg);
        peers.putIfAbsent(msg.src, peer);
    }

    /**
     * Start listening to incoming UDP messages
     * @param port The port where to listen for incoming UDP traffic
     * @throws IOException In case the port cannot be opened
     */
    public void bindUdp(int port) throws IOException {
        imcTransport.bindUdp(port);
        StringBuilder buf = new StringBuilder(localNode.services);
        for (String itf : NetworkUtils.getNetworkInterfaces())
            buf.append("imc+udp://").append(itf).append(":").append(port).append("/;");
        localNode.services = buf.toString();
    }

    /**
     * Start listening to incoming TCP messages
     * @param port The port where to listen for incoming TCP traffic
     * @throws IOException In case the port cannot be opened
     */
    public void bindTcp(int port) throws IOException {
        imcTransport.bindTcp(port);
        StringBuilder buf = new StringBuilder(localNode.services);
        for (String itf : NetworkUtils.getNetworkInterfaces())
            buf.append("imc+tcp://").append(itf).append(":").append(port).append("/;");
        localNode.services = buf.toString();
    }

    /**
     * Start discovering other IMP peers using broadcast and multicast
     * @throws IOException In case cannot listen to broadcast / multicast traffic
     */
    public void bindDiscovery() throws IOException {
        imcTransport.bindDiscovery();
    }

    /**
     * Start listening (should be called after binding to UDP / TCP)
     * @see {@link #bindTcp(int)}
     * @see {@link #bindUdp(int)}
     * @see {@link #bindDiscovery()}
     */
    public void start() {
        imcTransport.start();
    }

    /**
     * Stop listening
     */
    public void stop() {
        imcTransport.interrupt();
        PeriodicCallbacks.unregister(this);
    }

    @Periodic(10_000)
    private void clearDeadPeers() {
        List<ImcPeer> deadPeers = peers.values().stream().filter(p -> !p.isAlive()).collect(Collectors.toList());
        deadPeers.forEach(d -> peers.remove(d.getId()));
    }

    @Periodic(10_000)
    private void sendAnnounce() {
        localNode.timestamp = System.currentTimeMillis() / 1000.0;
        try {
            ImcTransport.multicast(localNode);
            ImcTransport.broadcast(localNode);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Periodic
    private void sendHeartbeats() {
        Heartbeat hb = new Heartbeat();
        hb.src = localNode.src;
        peers.forEachValue(1, peer -> {
            try {
                if (peer.isActive())
                    ImcTransport.sendViaUdp(hb, peer.getUdpAddress().getHostName(), peer.getUdpAddress().getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        ImcNetwork network = new ImcNetwork("MyImcNode", 45645, SystemType.CCU);
        network.bindUdp(6009);
        network.bindTcp(7080);
        network.bindDiscovery();
        network.setConnectionPolicy(p -> p.getType() == SystemType.UUV);
        network.start();
    }
}
