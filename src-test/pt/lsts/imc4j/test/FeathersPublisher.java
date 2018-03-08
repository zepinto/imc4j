package pt.lsts.imc4j.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.eclipsesource.json.Json;
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
	String ripplesUrl = "http://ripples.lsts.pt/positions";
	
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
	
	double lastUpdate;
	private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	
	public FeathersPublisher(ActorContext context) throws JAXBException {
		super(context);
	}
	
	@Periodic(1000)
	public void updatePosition() {
		double deltaT = (System.currentTimeMillis() - lastUpdate) / 1000.0;
		double[] newPos = WGS84Utilities.WGS84displace(latitude, longitude, 0, xSpeed * deltaT, ySpeed * deltaT, 0);
		latitude = newPos[0];
		longitude = newPos[1];
		Logger.getLogger(getClass().getSimpleName()).info(latitude+", "+longitude);
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
			Logger.getLogger(getClass().getSimpleName()).info("Posting "+info);
			send(info);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	@Periodic(10_000)
	public void publishToRipples() {
		
		JsonObject obj = new JsonObject();
		obj.add("imcid", imcId);
		obj.add("name", sysName);
		obj.add("coordinates", new JsonArray().add(latitude).add(longitude));
		obj.add("iridium", "");
		obj.add("created_at", fmt.format(new Date()));
		obj.add("updated_at", fmt.format(new Date()));
		System.out.println(obj.toString());
	}
	
	public static void main(String[] args) throws Exception {
		AbstractActor.exec(FeathersPublisher.class);
	}
}
