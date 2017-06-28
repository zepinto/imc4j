package pt.lsts.backseat.distress.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;


public class TCPConnection {
    
    private String tcpHost;
    private int tcpPort;

    private Socket tcpSocket = null;
    private boolean isTcpConnected = false;
    
    private boolean connected = false;

    @FunctionalInterface
    public static interface Listener {
        public void process(String sentence);
    }

    private HashSet<Listener> listeners = new HashSet<>();

    public TCPConnection(String tcpHost, int tcpPort) {
        this.tcpHost = tcpHost;
        this.tcpPort = tcpPort;
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

    private void setTcpConnected(boolean isTcpConnected) {
        this.isTcpConnected = isTcpConnected;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void connect() {
        connected = true;

        try {
            final Socket socket = new Socket();
            this.tcpSocket = socket;
            Thread listener = new Thread("NMEA TCP Listener") {
                public void run() {
                    BufferedReader reader = null;
                    try {
                        socket.setSoTimeout(1000);
                    }
                    catch (SocketException e1) {
                        e1.printStackTrace();
                    }
                    try {
                        socket.connect(new InetSocketAddress(tcpHost, tcpPort));
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        setTcpConnected(true);
                    }
                    catch (ConnectException e) {
                        System.out.println(TCPConnection.this.getClass().getSimpleName()
                                + "Error connecting via TCP to " + tcpHost + ":" + tcpPort + " :: " + e.getMessage());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(TCPConnection.this.getClass().getSimpleName()
                                + "Error connecting via TCP to " + tcpHost + ":" + tcpPort + " :: " + e.getMessage());
                        
                        setTcpConnected(false);

                        // if still connected, we need to reconnect
                        reconnect(socket);
                        
                        return;
                    }
                    System.out.println("Listening to NMEA messages over TCP " + tcpHost + ":" + tcpPort);
                    while (connected && isTcpConnected && tcpSocket.isConnected()) {
                        try {
                            String sentence = reader.readLine();
                            if (sentence == null)
                                break;
                            if (sentence.isEmpty())
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
                    System.out.println("TCP Socket closed.");
                    try {
                        socket.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        setTcpConnected(false);
                    }
                    
                    // if still connected, we need to reconnect
                    reconnect(socket);
                }

                private void reconnect(final Socket socket) {
                    if (connected) {
                        try {
                            Thread.sleep(5000);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (connected && !isTcpConnected && socket == TCPConnection.this.tcpSocket) {
                            connect();
                        }
                    }
                };
            };
            listener.setDaemon(true);
            setTcpConnected(true);
            listener.start();
            
            connected = isTcpConnected;
        }
        catch (Exception e) {
            connected = false;
        }
    }

    public void disconnect() throws Exception {
        connected = false;
        if (tcpSocket != null) {
            tcpSocket.close();
            setTcpConnected(false);
            tcpSocket = null;
        }
        if (tcpSocket == null)
            setTcpConnected(false);

        connected = isTcpConnected;
    }

    public boolean send(String msg) {
        try {
            return send(msg.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return send(msg.getBytes());
        }
    }

    public boolean send(byte[] b) {
        if (tcpSocket != null && connected && tcpSocket.isConnected()) {
            try {
                OutputStream output = tcpSocket.getOutputStream();
                output.write(b);
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
