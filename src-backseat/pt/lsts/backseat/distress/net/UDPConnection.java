package pt.lsts.backseat.distress.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;

public class UDPConnection {
    
    private int udpPort;

    private DatagramSocket udpSocket = null;
    private boolean isUdpConnected = false;
    
    private boolean connected = false;

    @FunctionalInterface
    public static interface Listener {
        public void process(String sentence);
    }

    private HashSet<Listener> listeners = new HashSet<>();

    public UDPConnection(int udpPort) {
        this.udpPort = udpPort;
    }

    public void register(Listener list) {
        synchronized (listeners) {
            if (!listeners.contains(list))
                listeners.add(list);
        }
    }

    public void unRegister(Listener list) {
        synchronized (listeners) {
            listeners.remove(list);
        }
    }

    private void setUdpConnected(boolean isUdpConnected) {
        this.isUdpConnected = isUdpConnected;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void connect() {
        connected = true;

        try {
            final DatagramSocket socket = new DatagramSocket(udpPort);
            this.udpSocket = socket;
            Thread udpListenerThread = new Thread("NMEA UDP Listener") {
                public void run() {
                    setUdpConnected(true);

                    try {
                        socket.setSoTimeout(1000);
                    }
                    catch (SocketException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("Listening to NMEA messages over UDP " + udpPort);
                    while (connected && isUdpConnected) {
                        try {
                            DatagramPacket dp = new DatagramPacket(new byte[65507], 65507);
                            socket.receive(dp);
                            String sentence = new String(dp.getData());
                            sentence = sentence.substring(0, sentence.indexOf(0));
                            if (sentence == null || sentence.isEmpty())
                                continue;
                            try {
                                synchronized (listeners) {
                                    for (Listener list : listeners) {
                                        list.process(sentence);
                                    }
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
//                            if (logReceivedData)
//                                LsfMessageLogger.log(new DevDataText(sentence));
                        }
                        catch (SocketTimeoutException e) {
                            continue;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                    System.out.println("NMEA :: " + "Stop listening via UDP.");
                    socket.close();
                    setUdpConnected(false);
                };
            };
            udpListenerThread.setDaemon(true);
            setUdpConnected(true);
            udpListenerThread.start();
            
            connected = isUdpConnected;
        }
        catch (Exception e) {
            connected = false;
        }
    }

    public void disconnect() throws Exception {
        connected = false;
        if (udpSocket != null) {
            udpSocket.close();
            setUdpConnected(false);
            udpSocket = null;
        }
        if (udpSocket == null)
            setUdpConnected(false);

        connected = isUdpConnected;
    }
}
