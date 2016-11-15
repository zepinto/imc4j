package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Rows maneuver (i.e: lawn mower type maneuver)
 */
public class Rows extends Maneuver {
	public static final int ID_STATIC = 456;

	/**
	 * The amount of time the maneuver is allowed to run.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int timeout = 0;

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
	 * Maneuver speed reference.
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
	 * Rows bearing angle.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 6.283185307179586,
			min = 0,
			units = "rad"
	)
	public double bearing = 0;

	/**
	 * Rows cross angle reference.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.047197551197,
			min = -1.047197551197,
			units = "rad"
	)
	public double cross_angle = 0;

	/**
	 * Width of the maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float width = 0f;

	/**
	 * Length of the maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float length = 0f;

	/**
	 * Horizontal step.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float hstep = 30f;

	/**
	 * Desired curve offset.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "m"
	)
	public int coff = 0;

	/**
	 * Alternation parameter.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 100,
			units = "%"
	)
	public int alternation = 50;

	/**
	 * Maneuver flags.
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
	public String custom = "";

	public int mgid() {
		return 456;
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
			_out.writeFloat(speed);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			_out.writeDouble(bearing);
			_out.writeDouble(cross_angle);
			_out.writeFloat(width);
			_out.writeFloat(length);
			_out.writeFloat(hstep);
			_out.writeByte(coff);
			_out.writeByte(alternation);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
			SerializationUtils.serializePlaintext(_out, custom);
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
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			bearing = buf.getDouble();
			cross_angle = buf.getDouble();
			width = buf.getFloat();
			length = buf.getFloat();
			hstep = buf.getFloat();
			coff = buf.get() & 0xFF;
			alternation = buf.get() & 0xFF;
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
			custom = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FLAGS {
		FLG_SQUARE_CURVE(0x0001l),

		FLG_CURVE_RIGHT(0x0002l);

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
