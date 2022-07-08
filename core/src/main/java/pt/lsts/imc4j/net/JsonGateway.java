package pt.lsts.imc4j.net;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Message;

public class JsonGateway {

    private CopyOnWriteArrayList<NetSocket> jsonClients = new CopyOnWriteArrayList<>();
    private int tcpPort = 8009;

    public JsonGateway(int port) {
        this.tcpPort = port;
        ImcNetwork network = new ImcNetwork("JsonGateway", 404, SystemType.CCU);
        try {
            network.startListening(7001);
            network.setConnectionPolicy(p -> p.getName().contains("auv") || p.getName().contains("manta"));
            network.subscribe(Message.class, this::onMsg);
            initServer(tcpPort);
        }
        catch (Exception e) {
            Logger.getLogger(getClass().getSimpleName()).severe("Could not start gateway: "+e.getMessage());
        }
    }

    void onMsg(Message msg) {
        if (jsonClients.isEmpty())
            return;
        String data = msg.toString()+"\n";
        jsonClients.forEach(client -> client.write(data));    
    }

    // TCP server initialization
    void initServer(int tcpPort) throws Exception {
        NetServer server = Vertx.vertx().createNetServer();
        server.connectHandler(this::clientConnected);

        server.listen(tcpPort, res -> {
            if (res.succeeded()) {
                Logger.getLogger(getClass().getSimpleName())
                        .info("TCP server listening on port " + server.actualPort());
            } else {
                Logger.getLogger(JsonGateway.class.getSimpleName())
                        .warning("Failed to start IMCServer: " + res.cause().getMessage());
            }
        });
    }

    void clientConnected(NetSocket socket) {
        Logger.getLogger(JsonGateway.class.getSimpleName())
                .info("New client connected: " + socket);
        jsonClients.add(socket);
    }

    public static void main(String[] args) {
        int port = 8009;
        if (args.length >= 1)
            port = Integer.parseInt(args[0]);
        new JsonGateway(port);
    }
}