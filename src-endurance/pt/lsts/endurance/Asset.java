package pt.lsts.endurance;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import pt.lsts.imc4j.util.WGS84Utilities;

public class Asset {

	private final String assetName;
	private Plan plan = null;
	private Hashtable<String, Object> config = new Hashtable<>();
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
	
	public Hashtable<String, Object> getConfig() {
		return this.config;
	}	
}
