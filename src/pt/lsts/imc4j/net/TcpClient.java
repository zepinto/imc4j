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

	private Socket socket = null;
	private HashSet<ImcConsumer> consumers = new HashSet<ImcConsumer>();
	protected boolean connected = false;
	private InputStream input;
	private OutputStream output;
	public int remoteSrc = 0;
	public int localSrc = 0x555;

	public void connect(String host, int port) throws Exception {
		socket = new Socket(host, port);
		connected = true;
		this.input = socket.getInputStream();
		this.output = socket.getOutputStream();
		start();
	}

	@Override
	public void run() {
		while (connected) {
			synchronized (socket) {
				try {
					while (input.available() >= 22) {
						Message m = SerializationUtils.deserializeMessage(input);

						if (m != null) {
							if (remoteSrc == 0)
								remoteSrc = m.src;
							dispatch(m);
						}
					}
				} catch (Exception e) {
					try {
						socket.close();
						socket = null;
					}
					catch (Exception ex) {
						ex.printStackTrace();
					}
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				return;
			}
		}
	}

	public void sendOut(Message m) throws IOException {
		m.dst = 65535;
		m.src = remoteSrc;
		m.timestamp = System.currentTimeMillis()/1000.0;
		synchronized (socket) {
			try {
				output.write(m.serialize());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send(Message m) throws IOException {
		m.dst = remoteSrc;
		m.src = localSrc;
		m.timestamp = System.currentTimeMillis()/1000.0;
		
		synchronized (socket) {
			try {
				output.write(m.serialize());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void dispatch(Message m) {
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
}
