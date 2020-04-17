package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.TupleList;

/**
 * This maneuver is a mix between the Loiter maneuver and the YoYo maneuver.
 * The vehicle cirlcles around a specific waypoint with a variable Z
 * reference between a minimum and maximum value.
 */
public class CompassCalibration extends Maneuver {
	public static final int ID_STATIC = 475;

	/**
	 * The timeout indicates the time that an error should occur if
	 * exceeded.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int timeout = 0;

	/**
	 * WGS-84 Latitude coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Maneuver reference in the z axis. Use z_units to specify
	 * whether z represents depth, altitude or other.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * Units of the z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits z_units = ZUnits.values()[0];

	/**
	 * Pitch angle used to perform the maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public float pitch = 0f;

	/**
	 * Yoyo motion amplitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float amplitude = 0f;

	/**
	 * The duration of this maneuver. Use '0' for unlimited duration time.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int duration = 0;

	/**
	 * Maneuver speed.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float speed = 0f;

	/**
	 * Speed units.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SpeedUnits speed_units = SpeedUnits.values()[0];

	/**
	 * Radius of the maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 100000,
			min = 1,
			units = "m"
	)
	public float radius = 0f;

	/**
	 * Direction of the maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 3,
			units = "Enumerated"
	)
	public DIRECTION direction = DIRECTION.values()[0];

	/**
	 * Custom settings for maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList custom = new TupleList("");

	public String abbrev() {
		return "CompassCalibration";
	}

	public int mgid() {
		return 475;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(timeout);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(z);
			_out.writeByte((int)(z_units != null? z_units.value() : 0));
			_out.writeFloat(pitch);
			_out.writeFloat(amplitude);
			_out.writeShort(duration);
			_out.writeFloat(speed);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			_out.writeFloat(radius);
			_out.writeByte((int)(direction != null? direction.value() : 0));
			SerializationUtils.serializePlaintext(_out, custom == null? null : custom.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			timeout = buf.getShort() & 0xFFFF;
			lat = buf.getDouble();
			lon = buf.getDouble();
			z = buf.getFloat();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
			pitch = buf.getFloat();
			amplitude = buf.getFloat();
			duration = buf.getShort() & 0xFFFF;
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			radius = buf.getFloat();
			direction = DIRECTION.valueOf(buf.get() & 0xFF);
			custom = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum DIRECTION {
		LD_VDEP(0l),

		LD_CLOCKW(1l),

		LD_CCLOCKW(2l),

		LD_IWINDCURR(3l);

		protected long value;

		DIRECTION(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static DIRECTION valueOf(long value) throws IllegalArgumentException {
			for (DIRECTION v : DIRECTION.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for DIRECTION: "+value);
		}
	}
}
