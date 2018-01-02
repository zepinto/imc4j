package pt.lsts.endurance;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Generated;

public class AssetState {

	private final Date timestamp;
	private final double latitude;
	private final double longitude;
	private final double heading;
	private final double fuel;
	private final ArrayList<String> errors;

	@Generated("SparkTools")
	private AssetState(Builder builder) {
		this.timestamp = builder.timestamp;
		this.latitude = builder.latitude;
		this.longitude = builder.longitude;
		this.heading = builder.heading;
		this.fuel = builder.fuel;
		this.errors = builder.errors;
	}

	public final Date getTimestamp() {
		return timestamp;
	}

	public final double getLatitude() {
		return latitude;
	}

	public final double getLongitude() {
		return longitude;
	}

	public final double getHeading() {
		return heading;
	}

	public final double getFuel() {
		return fuel;
	}

	public final ArrayList<String> getErrors() {
		return errors;
	}

	/**
	 * Creates builder to build {@link AssetState}.
	 * 
	 * @return created builder
	 */
	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder to build {@link AssetState}.
	 */
	@Generated("SparkTools")
	public static final class Builder {
		private Date timestamp;
		private double latitude;
		private double longitude;
		private double heading;
		private double fuel;
		private ArrayList<String> errors;

		private Builder() {
		}

		public Builder withTimestamp(Date timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public Builder withLatitude(double latitude) {
			this.latitude = latitude;
			return this;
		}

		public Builder withLongitude(double longitude) {
			this.longitude = longitude;
			return this;
		}

		public Builder withHeading(double heading) {
			this.heading = heading;
			return this;
		}

		public Builder withFuel(double fuel) {
			this.fuel = fuel;
			return this;
		}

		public Builder withErrors(ArrayList<String> errors) {
			this.errors = errors;
			return this;
		}

		public AssetState build() {
			return new AssetState(this);
		}
	}

}
