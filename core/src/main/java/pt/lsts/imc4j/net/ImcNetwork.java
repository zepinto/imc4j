package pt.lsts.imc4j.net;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.*;
import pt.lsts.imc4j.util.ImcConsumable;
import pt.lsts.imc4j.util.NetworkUtils;
import pt.lsts.imc4j.util.PeriodicCallbacks;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class implements a connection to an IMC Network
 */
public class ImcNetwork implements ImcTransport.MsgHandler, ImcConsumable {
    private final ImcTransport imcTransport;
    private final Announce localNode;
    private final ConcurrentHashMap<Integer, ImcPeer> peers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ImcPeer> peersByName = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<? extends Message>, ArrayList<Consumer<? extends Message>>> consumers
            = new ConcurrentHashMap<>();
    Predicate<ImcPeer> connectionPolicy = p -> false;

    /**
     * Constructor creates a local IMC node (not connected initially).
     * @param sysName The name of the local IMC node
     * @param imcId The IMC ID for the local IMC node
     * @param sysType The type of system of the local IMC node
     * @see {@link #listenTcp(int)}
     * @see {@link #listenUdp(int)}
     * @see {@link #listenDiscovery()}
     */
    public ImcNetwork(String sysName, int imcId, SystemType sysType) {
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
                peer = new ImcPeer((Announce) msg, remoteAddress, localNode.src);
                if (connectionPolicy.test(peer))
                    peer.setActive(true);
                peersByName.put(((Announce)msg).sys_name, peer);
            }
            else
                return;
        }
        peer.setMessage(msg);
        peers.putIfAbsent(msg.src, peer);
        publish(msg);
    }

    /**
     * Start listening to incoming UDP messages
     * @param port The port where to listen for incoming UDP traffic
     * @throws IOException In case the port cannot be opened
     */
    public void listenUdp(int port) throws IOException {
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
    public void listenTcp(int port) throws IOException {
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
    public void listenDiscovery() throws IOException {
        imcTransport.bindDiscovery();
    }

    public void startListening(int port) throws IOException {
        listenUdp(port);
        listenTcp(port);
        listenDiscovery();
        start();
    }
    /**
     * Start listening (should be called after binding to UDP / TCP)
     * @see {@link #listenTcp(int)}
     * @see {@link #listenUdp(int)}
     * @see {@link #listenDiscovery()}
     */
    public void start() {
        if (!imcTransport.isAlive())
            imcTransport.start();
    }

    /**
     * Stop listening
     */
    public void stop() {
        imcTransport.interrupt();
        PeriodicCallbacks.unregister(this);
    }

    /**
     * Retrieve a peer by its ID.
     * @param imcId The IMC ID of the remote peer
     * @return The peer corresponding to the given ID
     * @throws Exception If the peer is not (currently) connected
     */
    public ImcPeer peer(int imcId) throws Exception {
        if (peers.containsKey(imcId))
            return peers.get(imcId);
        throw new Exception("Peer is not connected: "+imcId);
    }

    /**
     * Retrieve a peer by its Name.
     * @param name The name of the remote peer
     * @return The peer corresponding to the given name
     * @throws Exception If the peer is not (yet) connected
     */
    public ImcPeer peer(String name) throws Exception {
        if (peersByName.containsKey(name))
            return peersByName.get(name);
        throw new Exception("Peer is not connected: "+name);
    }

    @Periodic(10_000)
    private void clearDeadPeers() {
        List<ImcPeer> deadPeers = peers.values().stream().filter(p -> !p.isAlive()).collect(Collectors.toList());
        deadPeers.forEach(d -> {
            peers.remove(d.getId());
            peersByName.remove(d.getName());
        });
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
        peers.forEachValue(1, peer -> {
            try {
                if (peer.isActive() && peer.hasUdp())
                    peer.send(new Heartbeat());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Wait until a named peer becomes online
     * @param name The name of the peer to wait for
     * @return The ImcPeer after it has connected
     */
    private Future<ImcPeer> waitFor(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                while (true) {
                    if (peersByName.containsKey(name))
                        return peersByName.get(name);
                    Thread.sleep(100);
                }
            }
            catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        ImcNetwork network = new ImcNetwork("MyImcNode", 45645, SystemType.UUV);
        network.startListening(7001);
        network.setConnectionPolicy(p -> true);
        network.subscribe(Announce.class, ann -> {
            System.out.println(ann);
        });

        network.subscribe(Message.class, m -> System.out.println(m.abbrev() + " from " + m.src));
    }
}
