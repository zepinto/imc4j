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

/**
 * Concise representation of entire system state.
 */
public class StateReport extends Message {
	public static final int ID_STATIC = 514;

	/**
	 * Time, in seconds, since January 1st 1970.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "s"
	)
	public long stime = 0;

	/**
	 * Latitude of the system, in degrees.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "°"
	)
	public float latitude = 0f;

	/**
	 * Longitude of the system, in degrees.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "°"
	)
	public float longitude = 0f;

	/**
	 * Altitude of the system, in decimeters.
	 * * *0xFFFF* used for unknown / not applicable value.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "dm"
	)
	public int altitude = 0;

	/**
	 * Depth of the system, in decimeters.
	 * * *0xFFFF* used for unknown / not applicable value.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "dm"
	)
	public int depth = 0;

	/**
	 * Calculated as `(rads * (0xFFFF / (2 * PI))`
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int heading = 0;

	/**
	 * Speed of the system in centimeters per second.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "cm/s"
	)
	public int speed = 0;

	/**
	 * System fuel gauge.
	 * * *-1* means unknown fuel level.
	 */
	@FieldType(
			type = IMCField.TYPE_INT8,
			units = "%"
	)
	public int fuel = 0;

	/**
	 * Progress of execution or idle state.
	 * * *-1* means Service mode
	 * * *-2* means Boot mode
	 * * *-3* means Calibration mode
	 * * *-4* means Error mode
	 */
	@FieldType(
			type = IMCField.TYPE_INT8,
			units = "%"
	)
	public int exec_state = 0;

	/**
	 * Checksum of the plan being executed.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int plan_checksum = 0;

	public String abbrev() {
		return "StateReport";
	}

	public int mgid() {
		return 514;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)stime);
			_out.writeFloat(latitude);
			_out.writeFloat(longitude);
			_out.writeShort(altitude);
			_out.writeShort(depth);
			_out.writeShort(heading);
			_out.writeShort(speed);
			_out.writeByte(fuel);
			_out.writeByte(exec_state);
			_out.writeShort(plan_checksum);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			stime = buf.getInt() & 0xFFFFFFFF;
			latitude = buf.getFloat();
			longitude = buf.getFloat();
			altitude = buf.getShort() & 0xFFFF;
			depth = buf.getShort() & 0xFFFF;
			heading = buf.getShort() & 0xFFFF;
			speed = buf.getShort();
			fuel = buf.get();
			exec_state = buf.get();
			plan_checksum = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ALTITUDE {
		null_ALT_UNKNOWN(0xFFFFl);

		protected long value;

		ALTITUDE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ALTITUDE valueOf(long value) throws IllegalArgumentException {
			for (ALTITUDE v : ALTITUDE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ALTITUDE: "+value);
		}
	}

	public enum DEPTH {
		null_DEP_UNKNOWN(0xFFFFl);

		protected long value;

		DEPTH(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static DEPTH valueOf(long value) throws IllegalArgumentException {
			for (DEPTH v : DEPTH.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for DEPTH: "+value);
		}
	}

	public enum FUEL {
		null_FUEL_UNKNOWN(-1l);

		protected long value;

		FUEL(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static FUEL valueOf(long value) throws IllegalArgumentException {
			for (FUEL v : FUEL.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for FUEL: "+value);
		}
	}

	public enum EXEC_STATE {
		null_STATE_SERVICE(-1l),

		null_STATE_BOOT(-2l),

		null_STATE_CALIBRATION(-3l),

		null_STATE_ERROR(-4l);

		protected long value;

		EXEC_STATE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static EXEC_STATE valueOf(long value) throws IllegalArgumentException {
			for (EXEC_STATE v : EXEC_STATE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for EXEC_STATE: "+value);
		}
	}
}
