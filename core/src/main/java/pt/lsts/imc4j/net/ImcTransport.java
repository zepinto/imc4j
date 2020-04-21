package pt.lsts.imc4j.net;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class ImcTransport extends Thread {

    private final ConcurrentHashMap<SocketChannel, ByteBuffer> incomingData = new ConcurrentHashMap<>();
    private final ByteBuffer udpBuffer = ByteBuffer.allocate(64 * 1024),
            multicastBuffer = ByteBuffer.allocate(64 * 1024);

    private HashSet<MsgHandler> handlers = new HashSet<>();
    private Selector selector = null;
    private ServerSocketChannel tcpServer = null;
    private DatagramChannel udpServer = null, discovery = null;

    public ImcTransport() {
        setName("ImcConnection");
        try {
            selector = Selector.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bindTcp(int port) throws IOException {
        tcpServer = ServerSocketChannel.open();
        ServerSocket serverSocket = tcpServer.socket();
        serverSocket.bind(new InetSocketAddress(port));
        tcpServer.configureBlocking(false);
        tcpServer.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void bindUdp(int port) throws IOException {
        udpServer = DatagramChannel.open(StandardProtocolFamily.INET);
        udpServer.socket().bind(new InetSocketAddress(port));
        udpServer.configureBlocking(false);
        udpServer.register(selector, SelectionKey.OP_READ);
    }

    public int bindDiscovery() throws IOException {
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        NetworkInterface itf = null;
        while (nis.hasMoreElements()) {
            NetworkInterface nif = nis.nextElement();
            if (!nif.isLoopback() || itf == null && !nis.hasMoreElements())
                itf = nif;
        }
        discovery = DatagramChannel.open(StandardProtocolFamily.INET);
        discovery.setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, itf)
                .setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false)
                .setOption(StandardSocketOptions.SO_BROADCAST, true);
        InetAddress group = InetAddress.getByName("224.0.75.69");
        discovery.join(group, itf);
        discovery.configureBlocking(false);
        int discoveryPort;
        for (discoveryPort = 30100; discoveryPort <= 30105; discoveryPort++) {
            try {
                discovery.bind(new InetSocketAddress(discoveryPort));
                System.out.println("Discovery bound to port " + discoveryPort);
                break;
            } catch (Exception e) {
                // try next one
            }
        }

        discovery.register(selector, SelectionKey.OP_READ);
        return discoveryPort;
    }

    @Override
    public void run() {
        System.out.println("IMC Transport is running");
        while (true) {
            try {
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
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleAccept(ServerSocketChannel mySocket, SelectionKey key) throws IOException {
        SocketChannel client = mySocket.accept();
        System.out.println("Tcp client connected: " + client.getRemoteAddress());
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    private byte[] toArray(ByteBuffer buffer) {
        int pos = buffer.position();
        byte[] ret = new byte[pos];
        buffer.rewind();
        buffer.get(ret);
        return ret;
    }

    private void handleRead(SelectionKey key) throws IOException {

        if (key.channel() == udpServer) {
            byte[] buff;
            synchronized (udpBuffer) {
                udpBuffer.clear();
                udpServer.receive(udpBuffer);
                buff = toArray(udpBuffer);
            }
            try {
                if (buff.length >= 20) {
                    Message m = Message.deserialize(buff);
                    messageReceived(m, null);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (key.channel() == discovery) {
            byte[] buff;
            synchronized (multicastBuffer) {
                multicastBuffer.clear();
                discovery.receive(multicastBuffer);
                buff = toArray(multicastBuffer);
            }
            try {
                if (buff.length >= 20) {
                    Message m = Message.deserialize(buff);
                    messageReceived(m, null);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            SocketChannel client = (SocketChannel) key.channel();

            ByteBuffer clientBuffer = incomingData.getOrDefault(client, ByteBuffer.allocate(64 * 1024));
            int bytesRead = client.read(clientBuffer);
            // connection is closed
            if (bytesRead == -1) {
                System.out.println("Tcp client disconnected: " + client.getRemoteAddress());
                client.close();
                return;
            }
            try {
                if (clientBuffer.position() == 1) {
                    clientBuffer.clear();
                }

                if (clientBuffer.position() > 20) {
                    Message m = readMessage(clientBuffer, client.getRemoteAddress());
                    if (m != null)
                        clientBuffer.clear();
                }
            } catch (Exception e) {
                System.err.println("Error reading message from tcp peer: " + client.getRemoteAddress() + ": " + e.getMessage());
                e.printStackTrace();
                clientBuffer.clear();
            }

            incomingData.putIfAbsent(client, clientBuffer);
        }
    }

    private Message readMessage(ByteBuffer buffer, SocketAddress source) throws Exception {
        int pos = buffer.position();
        buffer.rewind();
        short s = buffer.getShort();

        if (s != Message.SYNC_WORD) {
            if (s == Short.reverseBytes(Message.SYNC_WORD))
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            else {
                throw new IOException(String.format("Invalid Synchronization number: %X", s));
            }
        }

        int mgid = buffer.getShort() & 0xFFFF;
        int size = buffer.getShort();
        // if message is not complete yet, return
        if (buffer.remaining() < 14 + size) {
            buffer.position(pos);
            return null;
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
        // FIXME: verify checksum
        int checksum = buffer.getShort();
        messageReceived(m, source);
        return m;
    }

    private void messageReceived(Message m, SocketAddress sender) {
        for (MsgHandler handler : handlers)
            handler.handleMessage(m, sender);
    }

    public static void sendViaUdp(Message m, String hostname, int port) throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.bind(null);
        ByteBuffer byteBuffer = ByteBuffer.wrap(m.serialize());
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        datagramChannel.send(byteBuffer, inetSocketAddress);
    }

    public static Future<Void> sendViaTcp(Message m, String hostname, int port) {
        return CompletableFuture.runAsync(() -> {
            try {
                SocketChannel tmpChannel = SocketChannel.open(new InetSocketAddress(hostname, port));
                ByteBuffer data = ByteBuffer.wrap(m.serialize());
                while (data.hasRemaining())
                    tmpChannel.write(data);
                tmpChannel.close();
            }
            catch (IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    public static void broadcast(Message m) throws IOException {
        InetAddress broadcast = InetAddress.getByName("255.255.255.255");
        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);

        byte[] buffer = m.serialize();

        for (int port = 30100; port <= 30105; port++) {
            DatagramPacket packet
                    = new DatagramPacket(buffer, buffer.length, broadcast, port);
            socket.send(packet);
        }
        socket.close();
    }

    public static void multicast(Message m) throws IOException {
        InetAddress multicastGroup = InetAddress.getByName("224.0.75.69");
        MulticastSocket socket = new MulticastSocket();
        socket.joinGroup(multicastGroup);

        byte[] buffer = m.serialize();

        for (int port = 30100; port <= 30105; port++) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastGroup, port);
            socket.send(packet);
        }
        socket.close();
    }

    public void addHandler(MsgHandler imcHandler) {
        HashSet<MsgHandler> newSet = new HashSet<>(handlers);
        newSet.add(imcHandler);
        handlers = newSet;
    }

    public void removeHandler(MsgHandler imcHandler) {
        HashSet<MsgHandler> newSet = new HashSet<>(handlers);
        newSet.remove(imcHandler);
        handlers = newSet;
    }

    public interface MsgHandler {
        default void handleMessage(Message msg, SocketAddress remoteAddress) { }
    }
}
