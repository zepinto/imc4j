package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.ZUnits;

/**
 * This maneuver is used to command the vehicle to arrive at some destination at
 * a specified absolute time.
 * The vehicle's speed will vary according to environment conditions and/or maneuver start time.
 */
public class ScheduledGoto extends Maneuver {
	public static final int ID_STATIC = 487;

	/**
	 * Unix timestamp, in seconds, for the arrival at the destination.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double arrival_time = 0;

	/**
	 * WGS-84 Latitude of target waypoint.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude of target waypoint.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Target reference in the z axis. Use z_units to specify
	 * whether z represents depth, altitude or other.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * Units of the destination z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits z_units = ZUnits.values()[0];

	/**
	 * Z reference to use while travelling to the destination.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float travel_z = 0f;

	/**
	 * Z reference units to use while travelling to the destination.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits travel_z_units = ZUnits.values()[0];

	/**
	 * What to do if the vehicle fails to arrive before or at the requested time.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public DELAYED delayed = DELAYED.values()[0];

	public String abbrev() {
		return "ScheduledGoto";
	}

	public int mgid() {
		return 487;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(arrival_time);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(z);
			_out.writeByte((int)(z_units != null? z_units.value() : 0));
			_out.writeFloat(travel_z);
			_out.writeByte((int)(travel_z_units != null? travel_z_units.value() : 0));
			_out.writeByte((int)(delayed != null? delayed.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			arrival_time = buf.getDouble();
			lat = buf.getDouble();
			lon = buf.getDouble();
			z = buf.getFloat();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
			travel_z = buf.getFloat();
			travel_z_units = ZUnits.valueOf(buf.get() & 0xFF);
			delayed = DELAYED.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum DELAYED {
		DBEH_RESUME(0l),

		DBEH_SKIP(1l),

		DBEH_FAIL(2l);

		protected long value;

		DELAYED(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static DELAYED valueOf(long value) throws IllegalArgumentException {
			for (DELAYED v : DELAYED.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for DELAYED: "+value);
		}
	}
}
