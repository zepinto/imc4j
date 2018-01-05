package pt.lsts.endurance;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import pt.lsts.imc4j.util.WGS84Utilities;

public class Asset implements Comparable<Asset>{

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
		try {
			json.add("name", assetName);
			
			if (plan != null) {
				json.add("plan", Json.parse(plan.toString()));
			}
			
			if (received != null) {				
				json.add("lastState", Json.parse(received.toString()));							
			}
			
			if (!config.isEmpty()) {
				JsonObject cfg = new JsonObject();
				for (Entry<String, String> entry : config.entrySet())
					cfg.set(entry.getKey(), entry.getValue());			
				json.add("config", cfg);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}
	
	public static Asset parse(String json) throws Exception {
		JsonObject obj = Json.parse(json).asObject();
		Asset asset = new Asset(obj.getString("name", ""));
		if (obj.get("plan") != null)
			asset.setPlan(Plan.parse(obj.get("plan").toString()));
		
		if (obj.get("lastState") != null) {
			asset.setState(AssetState.parse(obj.get("lastState").toString()));
		}
		
		if (obj.get("config") != null) {
			JsonObject cfg = obj.get("config").asObject();
			for (String key: cfg.names())
				asset.config.put(key, cfg.getString(key, ""));			
		}
		return asset;
	}
	
	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}
	
	@Override
	public int compareTo(Asset o) {
		return toString().compareTo(o.toString());
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	public static void main(String[] args) throws Exception {
		String cmd = "{\"abbrev\":\"SoiCommand\",\"timestamp\":1.515070019499E9,\"src\":0,\"src_ent\":0,\"dst\":65535,\"dst_ent\":255,\"type\":1,\"command\":1,\"settings\":\"\",\"plan\":{\"abbrev\":\"SoiPlan\",\"plan_id\":35039,\"waypoints\":[{\"abbrev\":\"SoiWaypoint\",\"lat\":41.185413,\"lon\":-8.705885,\"eta\":0,\"duration\":0},{\"abbrev\":\"SoiWaypoint\",\"lat\":41.182922,\"lon\":-8.703834,\"eta\":0,\"duration\":0},{\"abbrev\":\"SoiWaypoint\",\"lat\":41.18226,\"lon\":-8.706192,\"eta\":0,\"duration\":0},{\"abbrev\":\"SoiWaypoint\",\"lat\":41.184853,\"lon\":-8.706833,\"eta\":0,\"duration\":0},{\"abbrev\":\"SoiWaypoint\",\"lat\":41.185413,\"lon\":-8.705894,\"eta\":0,\"duration\":0}]},\"info\":\"\"}";
		JsonObject obj = Json.parse(cmd).asObject();
		Asset asset = new Asset("lauv-xplore-1");
		asset.setPlan(Plan.parse(obj.get("plan").asObject().toString()));
		AssetState state = AssetState.builder()
				.withLatitude(41)
				.withLongitude(-8)
				.withTimestamp(new Date())
				.build();
		asset.setState(state);
		asset.config.put("max_depth", "50");
		
		System.out.println(asset.toString());	
		Asset asset2 = Asset.parse(asset.toString());
		System.out.println(asset2.toString());
		
		if (!asset.equals(asset2))
			throw new Exception("Assets to not match!");
	}
}
