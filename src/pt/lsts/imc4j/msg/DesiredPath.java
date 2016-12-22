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

/**
 * Perform path control.
 * The path is specified by two WGS-84 waypoints, respective
 * altitude / depth settings, optional loitering at the end point,
 * and some control flags.
 * The start and end waypoints are defined by the specified end point fields
 * ('end_{lat/lon/z}') plus:
 * 1. the start waypoint fields ('start_{lat|lon|z}') if the
 * 'START' flag (bit in 'flags' field) is set; or
 * 2. the end point of the previous path recently tracked; or
 * 3. the current location is the 'DIRECT' flag is set or if
 * the tracker has been idle for some time.
 * Altitude and depth control can be performed as follows:
 * 1. by default, the tracker will just transmit an altitude/depth
 * reference value equal to 'end_z' to the appropriate controller;
 * 2. if the 'NO_Z' flag is set no altitude/depth control will take
 * place, hence they can be controlled independently;
 * 3. if the '3DTRACK' flag is set, 3D-tracking will be done
 * (if supported by the active controller).
 * Loitering can be specified at the end point with a certain
 * radius ('lradius'), duration ('lduration'), and clockwise or
 * counter-clockwise direction ('CCLOCKW' flag).
 */
public class DesiredPath extends ControlCommand {
	public static final int ID_STATIC = 406;

	/**
	 * Unsigned integer reference for the scope of the desired path message.
	 * Path reference should only be set by a maneuver.
	 * Should be set to an always increasing reference at the time of dispatching this message.
	 * Lower level path controllers must inherit the same path reference sent by maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long path_ref = 0;

	/**
	 * WGS-84 latitude of start point. This will be ignored unless
	 * the 'START' flag is set.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double start_lat = 0;

	/**
	 * WGS-84 longitude of start point. This will be ignored unless
	 * the 'START' flag is set.
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
	 * Depth or altitude for the end point. This parameter will be
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
	 * Radius for loitering at end point. Specify less or equal to 0
	 * for no loitering.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float lradius = 0f;

	/**
	 * Desired Path flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	public String abbrev() {
		return "DesiredPath";
	}

	public int mgid() {
		return 406;
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
			_out.writeFloat(speed);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			_out.writeFloat(lradius);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
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
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			lradius = buf.getFloat();
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FLAGS {
		FL_START(0x01l),

		FL_DIRECT(0x02l),

		FL_NO_Z(0x04l),

		FL_3DTRACK(0x08l),

		FL_CCLOCKW(0x10l),

		FL_LOITER_CURR(0x20l),

		FL_TAKEOFF(0x40l),

		FL_LAND(0x80l);

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
