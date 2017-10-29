package pt.lsts.endurance;

import java.util.Date;
import java.util.LinkedHashMap;

import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.WGS84Utilities;

public class Asset {
	private final String assetName;
	private final AssetState state;
	private final AssetConfig config;
	private Plan plan = null;
	public static LinkedHashMap<String, Asset> assets = new LinkedHashMap<>();
	
	/**
	 * Constructs an asset with empty state and default configuration
	 * @param name
	 */
	private Asset(String name) {
		this.assetName = name;
		this.state = new AssetState();
		this.config = new AssetConfig();
	}
	
	/**
	 * Calculates when the vehicle should communicate again based on its plan
	 * @return The date of next communication (which can be in the past if the vehicle is delayed)
	 */
	public Date nextCommunication() {
		Date nextComm = new Date(state.getTimestamp());
		
		if (plan == null)
			return nextComm;
		
		
		for (Waypoint wpt : plan.getWaypoints()) {
			if (wpt.getArrivalTime().after(nextComm)) {
				nextComm = wpt.getArrivalTime();
			}
		}
		
		return nextComm;
	}
	
	/**
	 * Calculates an upcoming waypoint where the vehicle should be able to communicate with base station
	 * @return upcoming waypoint where the vehicle should be able to communicate with base station
	 */
	public AssetState futureState() {
		if (plan == null)
			return SerializationUtils.clone(state);
		
		Date now = new Date();
		Waypoint next = null;
		
		for (Waypoint wpt : plan.getWaypoints()) {
			if (wpt.getArrivalTime().after(now)) {
				next = wpt;
				break;
			}
		}
		
		AssetState ret = SerializationUtils.clone(state);
		ret.setTimestamp(next.getArrivalTime().getTime());
		ret.setLatitude(next.getLatitude());
		ret.setLongitude(next.getLongitude());
		
		return ret;
	}
	
	/**
	 * Calculates an estimated state for the vehicle based on its plan and current time
	 * @return estimated state for the vehicle based on its plan and current time
	 */
	public AssetState estimatedState() {
		if (plan == null)
			return SerializationUtils.clone(state);
		
		Date now = new Date();
		Waypoint previous = null;
		Waypoint next = null;		
		
		// find previous and following waypoint
		for (Waypoint wpt : plan.getWaypoints()) {
			if (previous == null || wpt.getArrivalTime().before(now))
				previous = wpt;
			if (next == null || wpt.getArrivalTime().after(now)) {
				next = wpt;
				break;
			}
		}
				
		if (previous == null || next == null || previous.compareTo(next) > 0)
			return null;
		
		// calculate where should the vehicle be between these two waypoints
		double totalTime = (next.getArrivalTime().getTime() - previous.getArrivalTime().getTime());
		double timeSincePrevious = (now.getTime() - previous.getArrivalTime().getTime());
		
		double offsets[] = WGS84Utilities.WGS84displacement(previous.getLatitude(), previous.getLongitude(), 0,
				next.getLatitude(), next.getLongitude(), 0);
		
		offsets[0] *= timeSincePrevious / totalTime;
		offsets[1] *= timeSincePrevious / totalTime;
		
		AssetState ret = SerializationUtils.clone(state);
		double[] pos = WGS84Utilities.WGS84displace(ret.getLatitude(), ret.getLongitude(), 0, offsets[0], offsets[1], 0);
		ret.setLatitude(pos[0]);
		ret.setLongitude(pos[1]);
		ret.setTimestamp(now.getTime());
		
		// calculate the heading
		ret.setHeading(Math.toDegrees(Math.atan2(offsets[0], offsets[1])));
		
		return ret;
	}
	
	/**
	 * @return the plan
	 */
	public final Plan getPlan() {
		return plan;
	}

	/**
	 * @param plan the plan to set
	 */
	public final void setPlan(Plan plan) {
		this.plan = plan;
	}

	/**
	 * @return the assetName
	 */
	public final String getAssetName() {
		return assetName;
	}

	/**
	 * @return the state
	 */
	public final AssetState getState() {
		return state;
	}

	/**
	 * @return the config
	 */
	public final AssetConfig getConfig() {
		return config;
	}

	public static Asset forName(String name) {
		synchronized (assets) {
			if (!assets.containsKey(name))
				assets.put(name, new Asset(name));
			return assets.get(name);
		}
	}
}
