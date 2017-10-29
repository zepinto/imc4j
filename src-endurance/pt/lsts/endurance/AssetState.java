package pt.lsts.endurance;

import java.util.LinkedList;

public class AssetState {
	private long timestamp = 0;
	private double latitude = 0, longitude = 0, heading = 0;
	private double fuel = 0;
	private final LinkedList<String> errors = new LinkedList<>();

	/**
	 * @return the timestamp
	 */
	public final long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public final void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the latitude
	 */
	public final double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            the latitude to set
	 */
	public final void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public final double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            the longitude to set
	 */
	public final void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return the heading
	 */
	public final double getHeading() {
		return heading;
	}

	/**
	 * @param heading
	 *            the heading to set
	 */
	public final void setHeading(double heading) {
		this.heading = heading;
	}

	/**
	 * @return the fuel
	 */
	public final double getFuel() {
		return fuel;
	}

	/**
	 * @param fuel
	 *            the fuel to set
	 */
	public final void setFuel(double fuel) {
		this.fuel = fuel;
	}

	/**
	 * @return the errors
	 */
	public final LinkedList<String> getErrors() {
		return errors;
	}

}
