package pt.lsts.imc4j.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.TimeZone;

import pt.lsts.imc4j.msg.LogBookEntry;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.ReportControl;
import pt.lsts.imc4j.util.ImcConsumer;
import pt.lsts.imc4j.util.PeriodicCallbacks;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * @author zp
 *
 */
public class TcpClient extends Thread {

	private Object lock = new Object();
    private Socket socket = null;
	private HashSet<ImcConsumer> consumers = new HashSet<ImcConsumer>();
	protected boolean connected = false;
	private InputStream input;
	private OutputStream output;
	public int remoteSrc = 0;
	public int localSrc = 0x555;
	
	private String host = "";
	private int port = 0;
	private int timeoutMillis = 5000;

	public void connect() throws Exception {
		connect("127.0.0.1", 6006);
	}
	
	public void connect(String host, int port) throws Exception {
        synchronized (lock) {
            this.host = host;
	        this.port = port;
	        try {
                reConnect(host, port);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
	        start();
        }
	}

    public void reConnect(String host, int port) throws Exception {
        synchronized (lock) {
            connected = true;
            socket = new Socket(host, port);
            socket.setSoTimeout(timeoutMillis);
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
        }
    }

	@Override
	public void run() {
		while (connected) {
			synchronized (lock) {
			    if (socket != null) {
			        try {
			            while (input.available() >= 22) {
			                Message m = SerializationUtils.deserializeMessage(input);
			                
			                if (m != null) {
			                    if (remoteSrc == 0)
			                        remoteSrc = m.src;
			                    dispatch(m);
			                }
			            }
			        } 
			        catch (Exception e) {
			            try {
			                socket.close();
			                socket = null;
			                input = null;
			                output = null;
			            }
			            catch (Exception ex) {
			                ex.printStackTrace();
			            }
			            e.printStackTrace();
			        }
			    }
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				return;
			}
			
			synchronized (lock) {
			    if (connected && socket == null) {
			        try {
			            Thread.sleep(5000);
			            if (connected && socket == null) {
			                print("Should be connected, retrying to connect.");
			                reConnect(host, port);
			            }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
			    }
			}
		}
	}

	public void send(Message m) throws IOException {
		if (m.dst == 0xFFFF)
			m.dst = remoteSrc;
		
		if (m.src == 0xFFFF)
			m.src = localSrc;
		
		m.timestamp = System.currentTimeMillis()/1000.0;
		
		synchronized (lock) {
			try {
				if (connected && socket != null && socket.isConnected()) {
				    output.write(m.serialize());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				
				// To force reconnect
				System.out.println("Force a reconnect due to error! " + e.getMessage()); // don't use print(..)!
				socket.close();
				socket = null;
			}
		}
	}

	public void dispatch(Message m) {
		for (ImcConsumer consumer : consumers)
			consumer.onMessage(m);		
	}

	public synchronized void register(Object pojo) {
		PeriodicCallbacks.register(pojo);
		consumers.add(ImcConsumer.create(pojo));
	}

	public synchronized void unregister(Object pojo) {
		PeriodicCallbacks.unregister(pojo);
		ImcConsumer c = null;
		for (ImcConsumer consumer : consumers) {
			if (consumer.getPojo() == pojo) {
				c = consumer;
				break;
			}
		}

		if (c != null)
			consumers.remove(c);
	}
	
	SimpleDateFormat sdf = new SimpleDateFormat("[YYYY-MM-dd HH:mm:ss.SS] ");
	public void print(String text) {
		
		LogBookEntry lbe = new LogBookEntry();
		lbe.text = text;
		lbe.htime = System.currentTimeMillis() / 1000.0;
		lbe.src = remoteSrc;
		lbe.type = LogBookEntry.TYPE.LBET_INFO;
		lbe.context = "Back Seat Driver";
		try {
			send(lbe);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.println(sdf.format(new Date()) + text);
	}
	
	public void sendReport(EnumSet<ReportControl.COMM_INTERFACE> interfaces) {
		ReportControl req = new ReportControl();
		req.src = localSrc;
		req.op = ReportControl.OP.OP_REQUEST_REPORT;
		req.comm_interface = interfaces;
		try {
			send(req);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void disconnect() {
		
		print("Terminating connection...");
		
		if (socket == null)
			return;
		
		synchronized (lock) {
			if (connected)
				try {
					socket.close();
				}
                catch (IOException e) {
                    e.printStackTrace();
                }
			consumers.clear();
			input = null;
			output = null;
			socket = null;			
		}		
		PeriodicCallbacks.unregister(this);
		connected = false;
	}
}
