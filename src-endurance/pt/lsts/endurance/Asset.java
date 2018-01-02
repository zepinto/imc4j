package pt.lsts.endurance;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import pt.lsts.imc4j.util.WGS84Utilities;

public class Asset {

	private final String assetName;
	private Plan plan = null;
	private Hashtable<String, String> config = new Hashtable<>();
	private AssetState received = null;
	
	public Asset(String assetName) {
		this.assetName = assetName;		
	}
	
	public final String getAssetName() {
		return assetName;
	}

	public Plan getPlan() {
		return this.plan;
	}
	
	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public AssetState receivedState() {
		return received;
	}
	
	public void setState(AssetState state) {
		if (received == null || state.getTimestamp().after(received.getTimestamp())) {
			received = state;
		}
	}
	
	public AssetState currentState() {
		
		if (plan == null)
			return received;
		
		AssetState past = receivedState();
		AssetState future = futureState();
		
		double deltaTime = (future.getTimestamp().getTime() - past.getTimestamp().getTime()) / 1000.0;
		
		
		if (deltaTime < 0)
			return past;
		
		double timeSince = (System.currentTimeMillis() - past.getTimestamp().getTime()) / 1000.0;
		timeSince = Math.max(timeSince, 0);
		
		double offsets[] = WGS84Utilities.WGS84displacement(past.getLatitude(), past.getLongitude(), 0, 
				future.getLatitude(), future.getLongitude(), 0);
		
		double curPos[] = WGS84Utilities.WGS84displace(past.getLatitude(), past.getLongitude(), 0,
				offsets[0] * (timeSince / deltaTime),
				offsets[1] * (timeSince / deltaTime),
				0);
		
		double heading = Math.toDegrees(Math.atan2(offsets[1], offsets[0]));
		
		return AssetState.builder()
					.withLatitude(curPos[0])
					.withLongitude(curPos[1])
					.withHeading(heading)
					.withTimestamp(new Date()).build();
	}
	
	public AssetState futureState() {
		
		if (receivedState() == null)
			return null;
				
		if (plan == null)
			return receivedState();
		
		Date now = new Date();
		
		List<Waypoint> wpts = plan.waypoints();
		
		Waypoint next = wpts.get(wpts.size()-1);
		Date arrivalTime = now;
		
		for (Waypoint wpt : wpts) {
			if (wpt.getArrivalTime().after(now)) {
				next = wpt;
				arrivalTime = wpt.getArrivalTime();
				break;
			}
		}
		
		return AssetState.builder()
				.withLatitude(next.getLatitude())
				.withLongitude(next.getLongitude())
				.withTimestamp(arrivalTime)
				.build(); 
	}	
	
	public Hashtable<String, String> getConfig() {
		return this.config;
	}	
	
	@Override
	public String toString() {
		JsonObject json = new JsonObject();
		json.add("name", assetName);
		
		if (plan != null)
			json.add("plan", Json.parse(plan.toString()));
		
		if (received != null) {
			JsonObject state = new JsonObject();
			state.add("time", received.getTimestamp().getTime()/1000.0);
			state.add("latitude", received.getLatitude());
			state.add("longitude", received.getLongitude());
			state.add("heading", received.getHeading());
			state.add("fuel", received.getFuel());
		
			if (!config.isEmpty()) {
				JsonObject cfg = new JsonObject();
				for (String key : config.keySet()) {
					cfg.add(key, config.get(key));
				}				
				state.add("config", cfg);
			}
			
			if (!received.getErrors().isEmpty()) {
				JsonArray array = new JsonArray();
				for (String err : received.getErrors())
					array.add(err);
				
				json.add("errors", array);
			}
			json.add("lastState", state);
		}
		
		
		
		return json.asString();
	}
	
	public static Asset parse(String json) throws Exception {
		JsonObject obj = Json.parse(json).asObject();
		Asset asset = new Asset(obj.getString("name", ""));
		if (obj.get("plan") != null)
			asset.plan = Plan.parse(obj.get("plan").asString());
		
		if (obj.get("config") != null) {
			JsonObject cfg = obj.get("config").asObject();
			for (String name : cfg.names())
				asset.config.put(name, cfg.getString(name, ""));						
		}
		
		if (obj.get("lastState") != null) {
			JsonObject state = obj.get("state").asObject();
			double lat = state.getDouble("latitude", 0);
			double lon = state.getDouble("longitude", 0);
			double heading = state.getDouble("heading", 0);
			double fuel = state.getDouble("fuel", 0);
			double timestamp = state.getDouble("time", 0);
			AssetState last = AssetState.builder()
					.withLatitude(lat)
					.withLongitude(lon)
					.withHeading(heading)
					.withFuel(fuel)
					.withTimestamp(new Date((long)(timestamp * 1000)))
					.build();
			asset.setState(last);
		}
		
		return asset;
	}
}
