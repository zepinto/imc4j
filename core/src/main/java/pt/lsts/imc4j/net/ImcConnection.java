package pt.lsts.imc4j.net;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.Heartbeat;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;
import pt.lsts.imc4j.util.NetworkUtils;
import pt.lsts.imc4j.util.PeriodicCallbacks;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ImcConnection {

    private final Announce localNode;
    private final ConcurrentHashMap<Integer, ImcPeer> peers;
    private final ConcurrentHashMap<SocketChannel, ByteBuffer> incomingData = new ConcurrentHashMap<>();
    private Selector selector = null;
    private ServerSocketChannel tcpServer = null;
    private DatagramChannel udpServer = null, discovery = null;
    private final ByteBuffer udpBuffer = ByteBuffer.allocate(64 * 1024),
            multicastBuffer = ByteBuffer.allocate(64 * 1024);

    private int localPort = -1;

    public ImcConnection(String sysName, SystemType sysType, int imcId) {
        localNode = new Announce();
        localNode.sys_name = sysName;
        localNode.sys_type = sysType;
        localNode.src = imcId;
        peers = new ConcurrentHashMap<>();
        PeriodicCallbacks.register(this);
    }

    public void setLocation(double latDegs, double lonDegs, float height) {
        localNode.lat = Math.toRadians(latDegs);
        localNode.lon = Math.toRadians(lonDegs);
        localNode.height = height;
    }

    public void setOwner(int id) {
        localNode.owner = id;
    }

    public void bind(int port) throws Exception {
        if (selector != null)
            selector.close();

        selector = Selector.open();

        this.localPort = port;

        localNode.services = "";

        for (String itf : NetworkUtils.getNetworkInterfaces()) {
            localNode.services += "imc+udp://" + itf + ":" + localPort + "/;";
            localNode.services += "imc+tcp://" + itf + ":" + localPort + "/;";
        }

        tcpServer = ServerSocketChannel.open();
        ServerSocket serverSocket = tcpServer.socket();
        serverSocket.bind(new InetSocketAddress(port));
        tcpServer.configureBlocking(false);
        tcpServer.register(selector, SelectionKey.OP_ACCEPT);

        udpServer = DatagramChannel.open(StandardProtocolFamily.INET);
        udpServer.socket().bind(new InetSocketAddress(port));
        udpServer.configureBlocking(false);
        udpServer.register(selector, SelectionKey.OP_READ);

        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface itf = null;
        while (nis.hasMoreElements()) {
            NetworkInterface nif = nis.nextElement();
            if (nif.isLoopback() && nis.hasMoreElements() || itf != null)
                continue;
            else
                itf = nif;
        }
        discovery = DatagramChannel.open(StandardProtocolFamily.INET);
        discovery.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, itf)
                .setOption(StandardSocketOptions.SO_BROADCAST, true);
        InetAddress group = InetAddress.getByName("224.0.75.69");
        discovery.join(group, itf);
        discovery.configureBlocking(false);

        for (int dport = 30100; dport <= 30105; dport++) {
            try {
                discovery.bind(new InetSocketAddress(dport));
                System.out.println("Discovery bound to port "+dport);
                break;
            }
            catch (Exception e) {
                continue;
            }
        }
        discovery.register(selector, SelectionKey.OP_READ);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> i = selectedKeys.iterator();

            while (i.hasNext()) {
                SelectionKey key = i.next();
                if (key.isAcceptable()) {
                    handleAccept(tcpServer, key);
                } else if (key.isReadable()) {
                    handleRead(key);
                }
                i.remove();
            }
        }
    }

    private void startDiscovery() {

    }

    private void handleAccept(ServerSocketChannel mySocket, SelectionKey key) throws IOException {
        SocketChannel client = mySocket.accept();
        System.out.println("Client connected: " + client.getRemoteAddress());
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {

        if (key.channel() == udpServer) {
            udpServer.receive(udpBuffer);
            try {
                readMessage(udpBuffer, udpServer.getRemoteAddress());
                udpBuffer.clear();
            } catch (Exception e) {
                System.err.println("Error reading message from " + udpServer.getRemoteAddress() + ": " + e.getMessage());
            }
        }
        else if (key.channel() == discovery) {
            discovery.receive(multicastBuffer);
            try {
                readMessage(multicastBuffer, discovery.getRemoteAddress());
                multicastBuffer.clear();
            } catch (Exception e) {
                System.err.println("Error reading message from " + discovery.getRemoteAddress() + ": " + e.getMessage());
            }
        }
        else {
            SocketChannel client = (SocketChannel) key.channel();

            ByteBuffer clientBuffer = incomingData.getOrDefault(client, ByteBuffer.allocate(64 * 1024));
            int bytesRead = client.read(clientBuffer);
            // connection is closed
            if (bytesRead == -1) {
                System.out.println("Client disconnected: " + client.getRemoteAddress());
                client.close();
                return;
            }
            try {
                readMessage(clientBuffer, client.getRemoteAddress());
            } catch (Exception e) {
                System.err.println("Error reading message from " + client.getRemoteAddress() + ": " + e.getMessage());
                clientBuffer.clear();
            }
            incomingData.putIfAbsent(client, clientBuffer);
        }
    }

    private void readMessage(ByteBuffer buffer, SocketAddress source) throws Exception {
        int pos = buffer.position();
        // header cannot be read yet
        if (pos < 20)
            return;

        buffer.position(0);
        short s = buffer.getShort();
        if (s != Message.SYNC_WORD) {
            if (s == Short.reverseBytes(Message.SYNC_WORD))
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            else
                throw new IOException("Invalid Synchronization number");
        }

        int mgid = buffer.getShort() & 0xFFFF;
        int size = buffer.getShort();
        // if message is not complete yet, return
        if (buffer.remaining() < 14 + size) {
            buffer.position(pos);
            return;
        }

        Message m = MessageFactory.create(mgid);
        if (m == null)
            throw new Exception("Unknown message type: " + mgid);

        m.timestamp = buffer.getDouble();
        m.src = buffer.getShort() & 0xFFFF;
        m.src_ent = buffer.get() & 0xFF;
        m.dst = buffer.getShort() & 0xFFFF;
        m.dst_ent = buffer.get() & 0xFF;
        m.deserializeFields(buffer);
        int checksum = buffer.getShort();
        buffer.clear();
        handleMessage(m, source);
    }

    private void handleMessage(Message m, SocketAddress sender) {
        ImcPeer peer = peers.getOrDefault(m.src, new ImcPeer(m.src));
        peer.setMessage(m, sender);
        peers.putIfAbsent(m.src, peer);
    }

    @Periodic(30_000)
    private void clearDeadPeers() {
        List<ImcPeer> deadPeers = peers.values().stream().filter(p -> !p.isAlive()).collect(Collectors.toList());
        deadPeers.forEach(d -> {
            peers.remove(d.getRemoteId());
        });
    }
    public void broadcast(Message m) {
        try {
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.bind(null);

            for (int port = 30100; port <= 30105; port++) {
                datagramChannel.setOption(StandardSocketOptions.SO_BROADCAST, true);
                ByteBuffer byteBuffer = ByteBuffer.wrap(m.serialize());
                InetSocketAddress inetSocketAddress = new
                        InetSocketAddress("255.255.255.255", port);
                datagramChannel.send(byteBuffer, inetSocketAddress);
            }
        }
        catch (Exception e) {

        }
    }

    public void multicast(Message m) {
        String multicastAddress = "224.0.75.69";
        try {
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.bind(null);
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface itf = nis.nextElement();
                if (itf.isLoopback() && nis.hasMoreElements())
                    continue;
                for (int port = 30100; port <= 30105; port++) {
                    datagramChannel.setOption(StandardSocketOptions
                            .IP_MULTICAST_IF, itf);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(m.serialize());
                    InetSocketAddress inetSocketAddress = new
                            InetSocketAddress(multicastAddress, port);
                    datagramChannel.send(byteBuffer, inetSocketAddress);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Periodic(10_000)
    private void sendAnnounce() {
        System.out.println("Send announces");
        localNode.timestamp = System.currentTimeMillis()/1000.0;
        multicast(localNode);
        broadcast(localNode);
    }

    @Periodic(1_000)
    private void sendHeartbeats() {
        System.out.println("Send heartbeats");
        Heartbeat hb = new Heartbeat();
        hb.src = localNode.src;
        peers.forEachValue(3, peer -> {
            try {
                peer.dispatch(hb);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        ImcConnection conn = new ImcConnection("MySystem", SystemType.CCU, 45);
        try {
            conn.bind(6007);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
