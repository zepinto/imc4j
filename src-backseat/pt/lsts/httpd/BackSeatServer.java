package pt.lsts.httpd;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import pt.lsts.autonomy.MissionExecutive;
import pt.lsts.backseat.BackSeatDriver;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.net.TcpClient;
import pt.lsts.imc4j.util.PeriodicCallbacks;
import pt.lsts.imc4j.util.PojoConfig;

public class BackSeatServer extends NanoHTTPD {

    protected enum BackSeatType {
        None,
        BackSeatDriver,
        MissionExecutive
    }
    
    @Parameter(description = "Auto Start on Power On")
    public boolean autoStartOnPowerOn = false;

    protected TcpClient driver;
    protected File output;
    protected BackSeatType type;
    protected String name;

    protected File configServerFile = new File(this.getClass().getSimpleName()+".ini");

	public BackSeatServer(TcpClient back_seat, int http_port) {
		super(http_port);
		
		if (configServerFile.exists()) {
		    try {
		        loadServerSettings(new String(Files.readAllBytes(configServerFile.toPath())));
		    }
		    catch (Exception e) {
		        e.printStackTrace();
		    }
		}       
		
		this.driver = back_seat;
		fillType();
		
		switch (type) {
            case MissionExecutive:
                ((MissionExecutive) driver).setUseSystemExitOrStop(false);
                break;
            default:
                break;
        }
		
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
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	stop();
		        System.out.println("Server is stopped.\n");
		    }
		}));
		
        System.out.println("Listening on port "+http_port+"...\n");

		try {
			start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);			
        }
		catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }
		
		if (autoStartOnPowerOn) {
            try {
                Thread.sleep(5000);
            }
            catch (Exception e) {
            }
            
            try {
                startBackSeat();
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
		}
		
		while(true) {
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {
			}
		}
	}

	private void fillType() {
	    name = driver.getClass().getSimpleName();
	    name = name.replaceAll("([a-z0-9])([A-Z])", "$1 $2");

	    if (driver instanceof BackSeatDriver)
	        type = BackSeatType.BackSeatDriver;
	    else if (driver instanceof MissionExecutive)
	        type = BackSeatType.MissionExecutive;
	    else
	        type = BackSeatType.None;
    }

    private String settings() throws Exception {
		StringBuilder sb = new StringBuilder();

		for (Field f : driver.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			f.setAccessible(true);
			if (p != null) {
				sb.append("# " + p.description() + "\n");
				Object value = f.get(driver);
				if (value instanceof String[])
                    value = String.join(", ", ((String[]) value));
				sb.append(f.getName() + "=" + value + "\n\n");
			}
		}

		return sb.toString();
	}

    private void loadServerSettings(String settings) throws Exception {
        if (settings == null || settings.isEmpty())
            return;

        Properties props = new Properties();
        props.load(new ByteArrayInputStream(settings.getBytes()));
        PojoConfig.setProperties(this, props);
    }

	private void loadSettings(String settings) throws Exception {
		if (settings == null || settings.isEmpty())
			return;

		Properties props = new Properties();
		props.load(new ByteArrayInputStream(settings.getBytes()));
		PojoConfig.setProperties(driver, props);
	}

	private void startBackSeat() throws Exception {
		switch (type) {
            case MissionExecutive:
                ((MissionExecutive) driver).setup();
                break;
            default:
                break;
        }
	    
	    driver.connect();
	    
        switch (type) {
            case MissionExecutive:
                ((MissionExecutive) driver).launch();
                break;
            default:
                break;
        }
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
		switch (type) {
		    case MissionExecutive:
		        ((MissionExecutive) driver).setUseSystemExitOrStop(false);
		        break;
		    default:
		        break;
		}
	}

	@Override
	public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
			Map<String, String> files) {

		if (uri.equals("/logbook")) {
			try {
				return newChunkedResponse(Status.OK, "text/plain", new FileInputStream(output));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (uri.equals("/style.css")) {
		    try {
		        InputStream stream = this.getClass().getResourceAsStream("style.css");
		        return newChunkedResponse(Status.OK, "text/css", stream);
		    }
		    catch (Exception e) {
		        e.printStackTrace();
		    }
		}

		if (uri.equals("/util.js")) {
		    try {
		        InputStream stream = this.getClass().getResourceAsStream("util.js");
		        return newChunkedResponse(Status.OK, "text/javascript", stream);
		    }
		    catch (Exception e) {
		        e.printStackTrace();
		    }
		}

        if (uri.equals("/manifest.json")) {
            try (InputStream inStream = this.getClass().getResourceAsStream("manifest.json");
                    Scanner s = new Scanner(inStream)) {
		        s.useDelimiter("\\A");
		        String manifest = s.hasNext() ? s.next() : "";

		        manifest = manifest.replaceFirst("\\$\\$SHORTNAME\\$\\$", name.replace(" ", "").substring(0, 12));
		        manifest = manifest.replaceFirst("\\$\\$NAME\\$\\$", name);
		        
		        return newChunkedResponse(Status.OK, "text/json", new ByteArrayInputStream(manifest.getBytes()));
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
		    }
		    catch (Exception e1) {
		        e1.printStackTrace();
		    }
			break;
		case "Stop":
			try {
				stopBackSeat();
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		case "Save":
			try {
				saveSettings(parms.get("settings"));
				
				String checkAutoStart = parms.get("autoStart");
				autoStartOnPowerOn = checkAutoStart != null && checkAutoStart.equalsIgnoreCase("checked");
				System.out.println(checkAutoStart);
				PojoConfig.writeProperties(this, configServerFile);
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
			break;
		default:
			break;
		}

		String settings = "";
		try {
			settings = settings();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		
        sb.append("<html lang=\"en\">");
        sb.append("<head>");
        sb.append("<title>").append(name).append("</title>");
        sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"/>");
        sb.append("<link rel=\"manifest\" href=\"/manifest.json\">");
        sb.append("<script src=\"util.js\"></script>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h1>").append(name).append("</h1>");
        sb.append("<form action=/ method='post'>\n");
		
		if (!driver.isAlive())
			sb.append("<input type='submit' name='cmd' value='Start' />");
		else
			sb.append("<input type='submit' name='cmd' value='Stop' />");
		
		sb.append(" &nbsp; <input type='submit' name='cmd' id='save' value='Save' />");
		
		String checkedAutoStart = autoStartOnPowerOn ? " checked=\"checked\"" : "";
		sb.append(" &nbsp; <label for=\"autoStart\"><input type=\"checkbox\" id=\"autoStart\" name=\"autoStart\""
		        + checkedAutoStart
		        + " value=\"checked\"/>Auto Start on Power On</label>");
		
		sb.append("<br/>");
		
		sb.append("<label for=\"settings\"><h2>Settings:</h2></label>");
		sb.append("<textarea class='settings' id='settings' name='settings' cols='100' rows='20'>\n");
		sb.append(settings);
		sb.append("</textarea>");

		if (driver.isAlive()) {
			sb.append("<h2><label for=\"logbook\">Log Book</label>:");
			sb.append("&nbsp; <input id='reloadLogbook' name='reloadLogbook' type=\"button\" onclick=\"reloadLogbookFrame()\" value=\"Reload\"><br/>");
            sb.append("</h2>");
			sb.append("<iframe onload='scrollToEnd();' name='logbook' id='logbook' title='log book' src='/logbook' width='600px'></iframe>");
		}

		sb.append("</form>\n");
		
        sb.append("</body>");
        sb.append("</html>");

		return newFixedLengthResponse(sb.toString());
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
            System.err.println("Usage: java -jar BackSeatServer.jar <class> <port>");
            System.err.println("    <class> - The full class name to run");
            System.err.println("    <port>  - The http server port for this service");
			System.exit(1);
		}
		
		new BackSeatServer((TcpClient) Class.forName(args[0]).newInstance(), Integer.parseInt(args[1]));
		System.exit(0);
	}
}
