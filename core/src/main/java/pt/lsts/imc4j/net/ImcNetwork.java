package pt.lsts.imc4j.net;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.Heartbeat;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.NetworkUtils;
import pt.lsts.imc4j.util.PeriodicCallbacks;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ImcNetwork implements ImcTransport.MsgHandler {
    private ImcTransport imcTransport;
    private final Announce localNode;
    private ConcurrentHashMap<Integer, ImcPeer> peers = new ConcurrentHashMap<>();

    private ImcNetwork(String sysName, int imcId, SystemType sysType) {
        localNode = new Announce();
        localNode.sys_name = sysName;
        localNode.sys_type = sysType;
        localNode.src = imcId;
        PeriodicCallbacks.register(this);
        imcTransport = new ImcTransport();
        imcTransport.setDaemon(true);
        imcTransport.addHandler(this);
    }

    public void setLocation(double latDegrees, double lonDegrees, float height) {
        localNode.lat = Math.toRadians(latDegrees);
        localNode.lon = Math.toRadians(lonDegrees);
        localNode.height = height;
    }

    public void setOwner(int id) {
        localNode.owner = id;
    }

    @Override
    public void handleMessage(Message msg, SocketAddress remoteAddress) {
        ImcPeer peer = peers.getOrDefault(msg.src, new ImcPeer(msg.src));
        peer.setMessage(msg, remoteAddress);
        peers.putIfAbsent(msg.src, peer);

        System.out.println("Received "+msg.abbrev());
    }

    public void bindUdp(int port) throws IOException {
        //if (!imcTransport.isAlive())
        //    imcTransport.start();
        imcTransport.bindUdp(port);
        for (String itf : NetworkUtils.getNetworkInterfaces())
            localNode.services += "imc+udp://" + itf + ":" + port + "/;";
    }

    public void bindTcp(int port) throws IOException {
        imcTransport.bindTcp(port);
        for (String itf : NetworkUtils.getNetworkInterfaces())
            localNode.services += "imc+tcp://" + itf + ":" + port + "/;";
    }

    public int bindDiscovery() throws IOException {
        return imcTransport.bindDiscovery();
    }

    public void start() {
        imcTransport.start();
    }

    public void stop() {
        PeriodicCallbacks.unregister(this);
    }

    @Periodic(10_000)
    private void clearDeadPeers() {
        List<ImcPeer> deadPeers = peers.values().stream().filter(p -> !p.isAlive()).collect(Collectors.toList());
        deadPeers.forEach(d -> peers.remove(d.getImcId()));
    }

    @Periodic(10_000)
    private void sendAnnounce() {
        System.out.println("Send announces");
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
        System.out.println("Send heartbeats");
        Heartbeat hb = new Heartbeat();
        hb.src = localNode.src;
        peers.forEachValue(3, peer -> {
            try {
                if (peer.isActive())
                    ImcTransport.sendViaUdp(hb, peer.getUdpAddress().getHostName(), peer.getUdpAddress().getPort());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) throws IOException {
        ImcNetwork network = new ImcNetwork("MyImcNode", 45645, SystemType.CCU);
        //network.bindUdp(6007);
        network.bindTcp(7080);
        network.bindDiscovery();
        network.start();
    }
}
