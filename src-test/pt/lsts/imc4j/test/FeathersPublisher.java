package pt.lsts.imc4j.test;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.msg.RemoteSensorInfo;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;
import pt.lsts.imc4j.util.WGS84Utilities;

public class FeathersPublisher extends AbstractActor {

	@Parameter
	String ripplesUrl = "http://ripples.lsts.pt/api/v1/systems";

	@Parameter
	double latitude = 41;

	@Parameter
	double longitude = -8;

	@Parameter
	double xSpeed = 0.5;

	@Parameter
	double ySpeed = -0.5;

	@Parameter
	String sysName = "feather-01";

	@Parameter
	int imcId = 20045;

	@Parameter
	String sensorClass = "Drifter";

	double lastUpdate = System.currentTimeMillis();
	private static final SimpleDateFormat fmt = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssZ");

	public FeathersPublisher(ActorContext context) throws JAXBException {
		super(context);
	}

	@Periodic(1000)
	public void updatePosition() {
		double deltaT = (System.currentTimeMillis() - lastUpdate) / 1000.0;
		double[] newPos = WGS84Utilities.WGS84displace(latitude, longitude, 0, xSpeed * deltaT, ySpeed * deltaT, 0);
		latitude = newPos[0];
		longitude = newPos[1];
		Logger.getLogger(getClass().getSimpleName()).info(latitude + ", " + longitude);
		lastUpdate = System.currentTimeMillis();
	}

	@Periodic(5_000)
	@Publish(RemoteSensorInfo.class)
	public void publishToNeptus() {
		RemoteSensorInfo info = new RemoteSensorInfo();
		info.lat = Math.toRadians(latitude);
		info.lon = Math.toRadians(longitude);
		info.heading = (float) Math.atan2(ySpeed, xSpeed);
		info.sensor_class = sensorClass;
		info.id = sysName;
		try {
			Logger.getLogger(getClass().getSimpleName()).info("Posting " + info);
			send(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Periodic(10_000)
	public void publishToRipples() throws Exception {

		JsonObject obj = new JsonObject();
		obj.add("imcid", imcId);
		obj.add("name", sysName);
		obj.add("coordinates", new JsonArray().add(latitude).add(longitude));
		obj.add("iridium", "");
		obj.add("created_at", fmt.format(new Date()));
		obj.add("updated_at", fmt.format(new Date()));

		URL url = new URL(ripplesUrl);
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		httpCon.setRequestProperty("Content-Type", "application/json");
		OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
		obj.writeTo(out);
		out.close();
		httpCon.getInputStream();

		Logger.getLogger(getClass().getSimpleName()).info("Response: "+httpCon.getResponseMessage());
	}

	public static void main(String[] args) throws Exception {
		AbstractActor.exec(FeathersPublisher.class);
	}
}
