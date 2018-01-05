package pt.lsts.endurance;

import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Generated;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class AssetState {

	private Date timestamp = null;
	private double latitude = 0;
	private double longitude = 0;
	private double heading = 0;
	private double fuel = 0;
	private ArrayList<String> errors = new ArrayList<>();

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
	
	@Override
	public String toString() {
		JsonObject state = new JsonObject();
		
		state.add("latitude", getLatitude());
		state.add("longitude", getLongitude());
		
		if (getTimestamp() != null)
			state.add("time", (int)(getTimestamp().getTime()/1000.0));
		
		if (getHeading() != 0)
			state.add("heading", getHeading());
		if (getFuel() != 0)
			state.add("fuel", getFuel());
		
		if (getErrors() != null && !getErrors().isEmpty()) {
			JsonArray array = new JsonArray();
			for (String err : getErrors())
				array.add(err);
			state.add("errors", array);
		}
		
		return state.toString();
	}
	
	public static AssetState parse(String json) {
		JsonObject obj = Json.parse(json).asObject();
		Date d = null;
		if (obj.get("time") != null)
			d = new Date((long)(obj.getDouble("time", 0) * 1000));
		
		ArrayList<String> errors = new ArrayList<>();
		
		if (obj.get("errors") != null) {
			for (JsonValue v : obj.get("errors").asArray().values()) {
				errors.add(v.asString());
			}			
		}
		
		return AssetState.builder()
				.withLatitude(obj.getDouble("latitude", 0))
				.withLongitude(obj.getDouble("longitude", 0))
				.withHeading(obj.getDouble("heading", 0))
				.withFuel(obj.getDouble("fuel", 0))
				.withErrors(errors)
				.withTimestamp(d)				
				.build();
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
	
	public static void main(String[] args) {
		AssetState asset = AssetState.builder()
				.withFuel(30)
				.withLatitude(41)
				.withLongitude(-8)
				.build();
		
		System.out.println(asset.toString());
		System.out.println(AssetState.parse(asset.toString()));
	}
}
