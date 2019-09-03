package pt.lsts.imc4j.net;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.util.FormatConversion;

public class SendImc {

	String host;
	int port;
	InputStream json;
	
	public SendImc(String host, int port, InputStream json) {
		this.host = host;
		this.port = port;
		this.json = json;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(json));
		String jsonContent = reader.lines().collect(Collectors.joining("\n"));
		try {
			Message msg = FormatConversion.fromJson(jsonContent);	
			UdpClient client = new UdpClient();
			client.connect(host, port);
			client.send(msg);
		}
		catch (Exception e) {
			System.err.println("Error reading message: "+e.getMessage());
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java -jar ImcSend.jar <host> <port> <file.json>");
			System.exit(1);
		}
		try {
			new SendImc(args[0],Integer.valueOf(args[1]), new FileInputStream(args[2]));	
		}
		catch (Exception e) {
			System.err.println("Arguments not valid: "+e.getMessage());
		}
	}
}
