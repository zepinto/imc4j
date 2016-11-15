package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.ZUnits;

/**
 * Path control state issued by Path Controller.
 */
public class PathControlState extends Message {
	public static final int ID_STATIC = 410;

	/**
	 * Unsigned integer reference of the desired path message to which this
	 * PathControlState message refers to.
	 * Path reference should only be set by a maneuver, not by path controllers.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long path_ref = 0;

	/**
	 * WGS-84 latitude of start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double start_lat = 0;

	/**
	 * WGS-84 longitude of start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double start_lon = 0;

	/**
	 * Altitude or depth of start point. This parameter will be
	 * ignored if the 'NO_Z' flag is set, or if the 'START' flag is
	 * not set.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float start_z = 0f;

	/**
	 * Units of the start point's z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits start_z_units = ZUnits.values()[0];

	/**
	 * WGS-84 latitude of end point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double end_lat = 0;

	/**
	 * WGS-84 longitude of end point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double end_lon = 0;

	/**
	 * Depth or altitude for the end point. This parameter should be
	 * ignored if the 'NO_Z' flag is set.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float end_z = 0f;

	/**
	 * Units of the end point's z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits end_z_units = ZUnits.values()[0];

	/**
	 * Radius for loitering at end point.
	 * Will be 0 if no loitering is active.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float lradius = 0f;

	/**
	 * Path control state flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	/**
	 * Along-Track position value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * Cross-Track position value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * Vertical-Track position value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * Along-Track velocity value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vx = 0f;

	/**
	 * Cross-Track velocity value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vy = 0f;

	/**
	 * Vertical-Track velocity value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vz = 0f;

	/**
	 * Course error value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float course_error = 0f;

	/**
	 * Estimated time to reach target waypoint. The value will be
	 * 65535 if the time is unknown or undefined, and 0 when
	 * loitering.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int eta = 0;

	public int mgid() {
		return 410;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)path_ref);
			_out.writeDouble(start_lat);
			_out.writeDouble(start_lon);
			_out.writeFloat(start_z);
			_out.writeByte((int)(start_z_units != null? start_z_units.value() : 0));
			_out.writeDouble(end_lat);
			_out.writeDouble(end_lon);
			_out.writeFloat(end_z);
			_out.writeByte((int)(end_z_units != null? end_z_units.value() : 0));
			_out.writeFloat(lradius);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			_out.writeFloat(vx);
			_out.writeFloat(vy);
			_out.writeFloat(vz);
			_out.writeFloat(course_error);
			_out.writeShort(eta);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			path_ref = buf.getInt() & 0xFFFFFFFF;
			start_lat = buf.getDouble();
			start_lon = buf.getDouble();
			start_z = buf.getFloat();
			start_z_units = ZUnits.valueOf(buf.get() & 0xFF);
			end_lat = buf.getDouble();
			end_lon = buf.getDouble();
			end_z = buf.getFloat();
			end_z_units = ZUnits.valueOf(buf.get() & 0xFF);
			lradius = buf.getFloat();
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
			vx = buf.getFloat();
			vy = buf.getFloat();
			vz = buf.getFloat();
			course_error = buf.getFloat();
			eta = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FLAGS {
		FL_NEAR(0x01l),

		FL_LOITERING(0x02l),

		FL_NO_Z(0x04l),

		FL_3DTRACK(0x08l),

		FL_CCLOCKW(0x10l);

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
