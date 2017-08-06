package pt.lsts.httpd;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.util.ServerRunner;
import pt.lsts.backseat.BackSeatDriver;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.util.PeriodicCallbacks;
import pt.lsts.imc4j.util.PojoConfig;

public class BackSeatServer extends NanoHTTPD {

	BackSeatDriver driver;
	File output;

	public BackSeatServer(BackSeatDriver back_seat, int http_port) {
		super(http_port);
		this.driver = back_seat;
		SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd_HHmmss");
		output = new File(sdf.format(new Date())+".log");
		File config = new File(back_seat.getClass().getSimpleName()+".ini");
		if (config.exists()) {
			try {
				loadSettings(new String(Files.readAllBytes(config.toPath())));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}		
		
		try {
			System.out.println("Redirecting output to "+output.getAbsolutePath());
			PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(output)), true);
			System.setOut(ps);
			System.setErr(ps);
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		ServerRunner.executeInstance(this);
		
	}

	private String settings() throws Exception {
		StringBuilder sb = new StringBuilder();

		for (Field f : driver.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			f.setAccessible(true);
			if (p != null) {
				sb.append("# " + p.description() + "\n");
				sb.append(f.getName() + "=" + f.get(driver) + "\n\n");
			}
		}

		return sb.toString();
	}

	private void loadSettings(String settings) throws Exception {
		if (settings == null || settings.isEmpty())
			return;

		Properties props = new Properties();
		props.load(new ByteArrayInputStream(settings.getBytes()));
		PojoConfig.setProperties(driver, props);
	}

	private void startBackSeat() throws Exception {
		driver.connect();
	}

	private void saveSettings(String settings) throws Exception {
		File config = new File(driver.getClass().getSimpleName()+".ini");
		Files.write(config.toPath(), settings.getBytes(), StandardOpenOption.CREATE);
	}
	
	private void stopBackSeat() throws Exception {
		PeriodicCallbacks.unregister(driver);
		driver.disconnect();
		driver.interrupt();
		driver = PojoConfig.create(driver.getClass(), PojoConfig.getProperties(driver));
	}

	@Override
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
			Map<String, String> files) {

		try {
			loadSettings(parms.get("settings"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (uri.equals("/logbook")) {
			try {
				return newChunkedResponse(Status.OK, "text/plain", new FileInputStream(output));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		String cmd = "none";

		if (parms.get("cmd") != null) {
			cmd = parms.get("cmd");
		}

		switch (cmd) {
		case "Start":
			try {
				startBackSeat();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		case "Stop":
			try {
				stopBackSeat();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		case "Save":
			try {
				saveSettings(parms.get("settings"));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		default:
			break;
		}

		String settings = "";
		try {
			settings = settings();
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<html>");
		sb.append("<head><title>" + driver.getClass().getSimpleName() + "</title></head>");
		sb.append("<body>");
		sb.append("<h1>" + driver.getClass().getSimpleName() + "</h1>");
		sb.append("<form action=/ method='post'>\n");
		
		if (!driver.isAlive())
			sb.append("<input type='submit' name='cmd' value='Start' />");
		else
			sb.append("<input type='submit' name='cmd' value='Stop' />");
		
		sb.append(" &nbsp; <input type='submit' name='cmd' value='Save' />");
		
		sb.append("<br/>");
		
		sb.append("<h2>Settings:</h2>");
		sb.append("<textarea class='settings' id='settings' name='settings' cols=100 rows=20>\n");
		sb.append(settings);
		sb.append("</textarea>");

		if (driver.isAlive()) {
			sb.append("<h2>Log book:</h2>");
			sb.append("<iframe src='/logbook' width='600px'></iframe>");
		}

		sb.append("</form>\n");

		return newFixedLengthResponse(sb.toString());

	}

	public static void main(String[] args) throws Exception {
		
		if (args.length != 2) {
			System.err.println("Usage: java -jar BackSeatServer.jar <class> <port>");
			System.exit(1);
		}
		
		new BackSeatServer((BackSeatDriver) Class.forName(args[0]).newInstance(), Integer.parseInt(args[1]));
		System.exit(0);
	}

}
