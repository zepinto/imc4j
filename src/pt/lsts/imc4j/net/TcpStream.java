package pt.lsts.imc4j.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.LsfReader;

public class TcpStream {

	private ServerSocket server;
	private ArrayList<ClientConnection> clients = new ArrayList<TcpStream.ClientConnection>();

	public TcpStream(String duneHostname, int dunePort, int serverPort) throws Exception {
		startServer(serverPort);
		TcpClient client = new TcpClient();
		new Thread(startServer(serverPort)).start();		
		client.register(this);
		System.out.println("connecting to "+duneHostname+":"+dunePort);
		client.connect(duneHostname, dunePort);
	}

	public TcpStream(File logFile, int serverPort) throws IOException, InterruptedException {
		LsfReader reader = new LsfReader(logFile);
		new Thread(startServer(serverPort)).start();
		Message message = reader.nextMessage();
		double startRealTime = System.currentTimeMillis();
		double startSimTime = message.timestamp;
		on(message);
		while ((message = reader.nextMessage()) != null) {
			double ellapsed = (System.currentTimeMillis() - startRealTime) / 1000.0;
			double simTime = startSimTime + ellapsed;
			
			while (message.timestamp > simTime) {
				Thread.sleep(10);
				ellapsed = (System.currentTimeMillis() - startRealTime) / 1000.0;
				simTime = startSimTime + ellapsed;
			}
				
			on(message);
		}
	}

	private Runnable startServer(int port) {
		return () -> {
			try {
				System.out.println("Starting server");
				server = new ServerSocket(port);
				
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try {
						server.close();
						clients.forEach(c -> {
							c.interrupt();
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}));

				while (true) {
					System.out.println("Waiting for connections");
					Socket connectionSocket = server.accept();
					ClientConnection conn = new ClientConnection(connectionSocket);
					clients.add(conn);
					conn.start();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}

	//private NumberFormat doubles = new DecimalFormat("0.00000000");
	//private NumberFormat floats = new DecimalFormat("0.000");

	private String toString(Message msg) {
		StringBuilder builder = new StringBuilder(msg.abbrev());
		builder.append(",").append(msg.timestamp);
		builder.append(",").append(msg.src);
		builder.append(",").append(msg.src_ent);
		builder.append(",").append(msg.dst);
		builder.append(",").append(msg.dst_ent);

		for (Field f : msg.getClass().getDeclaredFields()) {
			FieldType type = f.getAnnotation(FieldType.class);
			if (type == null)
				continue;
			try {
				switch (type.type()) {
				case TYPE_PLAINTEXT:
					builder.append(",").append(f.get(msg));
					break;
				case TYPE_MESSAGE:
					Message inline = (Message) f.get(msg);
					if (inline != null)
						builder.append(",").append(inline.abbrev());
					else
						builder.append(",").append("NULL");
					break;
				case TYPE_RAWDATA: {
					byte[] data = (byte[]) f.get(msg);
					builder.append(",").append(DatatypeConverter.printHexBinary(data));
					break;
				}
				case TYPE_MESSAGELIST: {
					builder.append(",MessageList[]");
					break;
				}
				/*case TYPE_FP32:
					builder.append(",").append(f.get(msg));
					break;
				case TYPE_FP64:
					builder.append(",").append(f.get(msg));
					break;*/
				default:
					builder.append(",").append(f.get(msg));
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return builder.toString();
	}

	@Consume
	public void on(Message msg) {
		String line = toString(msg);
		System.out.println(line);
		ArrayList<ClientConnection> defunct = new ArrayList<TcpStream.ClientConnection>();
		
		clients.forEach(c -> {
			try {
				if (!c.running)
					defunct.add(c);
				else
					c.send(line);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		for (ClientConnection c : defunct)
			clients.remove(c);
	}

	public static TcpStream create(String source, String destination) throws Exception {
		// connect to socket
		if (source.contains(":")) {
			String[] parts = source.split(":");
			return new TcpStream(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(destination));
		}
		// open file
		else {
			return new TcpStream(new File(source), Integer.parseInt(destination));
		}
	}

	public void on(Socket client, String command) {
		System.out.println(client.getRemoteSocketAddress() + " sent " + command);
	}

	class ClientConnection extends Thread {
		Socket clientSocket;
		BufferedWriter output;
		BufferedReader input;
		boolean running = false;

		public ClientConnection(Socket client) throws IOException {
			this.clientSocket = client;
			this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
		}

		public void send(String line) throws IOException {
			output.write(line + "\r\n");
		}

		@Override
		public void run() {
			running = true;
			while (true) {
				try {
					String line = input.readLine();
					if (line == null)
						break;
					else
						on(clientSocket, line);
				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
			running = false;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: java -jar TcpStream.jar <source> <port>");
			System.exit(1);
		}
		TcpStream.create(args[0], args[1]);
	}

}
