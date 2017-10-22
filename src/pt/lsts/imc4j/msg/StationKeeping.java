package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.TupleList;

/**
 * The Station Keeping maneuver makes the vehicle come to the surface
 * and then enter a given circular perimeter around a waypoint coordinate
 * for a certain amount of time.
 */
public class StationKeeping extends Maneuver {
	public static final int ID_STATIC = 461;

	/**
	 * WGS-84 Latitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude.
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
	 * Radius.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float radius = 0f;

	/**
	 * Duration (0 for unlimited).
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int duration = 0;

	/**
	 * The value of the desired speed, in the scale specified
	 * by the "Speed Units" field.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float speed = 0f;

	/**
	 * Indicates the units used for the speed value.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SpeedUnits speed_units = SpeedUnits.values()[0];

	/**
	 * The period at which the vehicle will popup to report its position.
	 * Only used if flag KEEP_SAFE is on.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int popup_period = 0;

	/**
	 * The duration of the station keeping at surface level when it pops up.
	 * Only used if flag KEEP_SAFE is on.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int popup_duration = 0;

	/**
	 * Flags of the maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	/**
	 * Custom settings for maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList custom = new TupleList("");

	public String abbrev() {
		return "StationKeeping";
	}

	public int mgid() {
		return 461;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(z);
			_out.writeByte((int)(z_units != null? z_units.value() : 0));
			_out.writeFloat(radius);
			_out.writeShort(duration);
			_out.writeFloat(speed);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			_out.writeShort(popup_period);
			_out.writeShort(popup_duration);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
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
			lat = buf.getDouble();
			lon = buf.getDouble();
			z = buf.getFloat();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
			radius = buf.getFloat();
			duration = buf.getShort() & 0xFFFF;
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			popup_period = buf.getShort() & 0xFFFF;
			popup_duration = buf.getShort() & 0xFFFF;
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
			custom = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FLAGS {
		FLG_KEEP_SAFE(0x01l);

		protected long value;

		FLAGS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static FLAGS valueOf(long value) throws IllegalArgumentException {
			for (FLAGS v : FLAGS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for FLAGS: "+value);
		}
	}
}
