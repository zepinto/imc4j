package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Vehicle opertional limits.
 * For aircraft this should represent the flight envelope and the dynamic contraints.
 */
public class VehicleOperationalLimits extends Message {
	public static final int ID_STATIC = 16;

	/**
	 * Action on the vehicle operation limits
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Minimum operation speed.
	 * For aircraft this is equal or larger then the stall speed.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m/s"
	)
	public float speed_min = 0f;

	/**
	 * Maximum operation speed.
	 * For aircraft this is limited by the engine power or structural contrains.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m/s"
	)
	public float speed_max = 0f;

	/**
	 * Maximum longitudinal acceleration.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m/s/s"
	)
	public float long_accel = 0f;

	/**
	 * Maximum altitude above mean-sea-level.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float alt_max_msl = 0f;

	/**
	 * Maximum dive rate (negative vertical speed) as a fraction of the longitudinal speed.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0
	)
	public float dive_fraction_max = 0f;

	/**
	 * Maximum climb rate (positive vertical speed) as a fraction of the longitudinal speed.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0
	)
	public float climb_fraction_max = 0f;

	/**
	 * Limit to the bank angle (roll; angle over the xx body-axis).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "rad"
	)
	public float bank_max = 0f;

	/**
	 * Limit to the bank angular rate (roll; angle over the xx body-axis).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "rad/s"
	)
	public float p_max = 0f;

	/**
	 * Minimum pitch angle (angle over the xx body-axis).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float pitch_min = 0f;

	/**
	 * Maximum pitch angle (angle over the xx body-axis).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float pitch_max = 0f;

	/**
	 * Maximum pitch angular rate (angle over the xx body-axis).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "rad/s"
	)
	public float q_max = 0f;

	/**
	 * Minimum load factor, i.e., maximum positive acceleration in the zz body-axis
	 * as a factor of the gravity acceleration at mean-sea-level.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 0,
			units = "g"
	)
	public float g_min = 0f;

	/**
	 * Maximum load factor, i.e., maximum negative acceleration in the zz body-axis
	 * as a factor of the gravity acceleration at mean-sea-level.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "g"
	)
	public float g_max = 0f;

	/**
	 * Maximum lateral load factor, i.e., maximum acceleration in the yy body-axis
	 * as a factor of the gravity acceleration at mean-sea-level.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "g"
	)
	public float g_lat_max = 0f;

	/**
	 * Minimum motor RPMs.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "rpm"
	)
	public float rpm_min = 0f;

	/**
	 * Maximum motor RPMs.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "rpm"
	)
	public float rpm_max = 0f;

	/**
	 * Maximum motor RPMs' rate of change.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "rpm/s"
	)
	public float rpm_rate_max = 0f;

	public int mgid() {
		return 16;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			_out.writeFloat(speed_min);
			_out.writeFloat(speed_max);
			_out.writeFloat(long_accel);
			_out.writeFloat(alt_max_msl);
			_out.writeFloat(dive_fraction_max);
			_out.writeFloat(climb_fraction_max);
			_out.writeFloat(bank_max);
			_out.writeFloat(p_max);
			_out.writeFloat(pitch_min);
			_out.writeFloat(pitch_max);
			_out.writeFloat(q_max);
			_out.writeFloat(g_min);
			_out.writeFloat(g_max);
			_out.writeFloat(g_lat_max);
			_out.writeFloat(rpm_min);
			_out.writeFloat(rpm_max);
			_out.writeFloat(rpm_rate_max);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			op = OP.valueOf(buf.get() & 0xFF);
			speed_min = buf.getFloat();
			speed_max = buf.getFloat();
			long_accel = buf.getFloat();
			alt_max_msl = buf.getFloat();
			dive_fraction_max = buf.getFloat();
			climb_fraction_max = buf.getFloat();
			bank_max = buf.getFloat();
			p_max = buf.getFloat();
			pitch_min = buf.getFloat();
			pitch_max = buf.getFloat();
			q_max = buf.getFloat();
			g_min = buf.getFloat();
			g_max = buf.getFloat();
			g_lat_max = buf.getFloat();
			rpm_min = buf.getFloat();
			rpm_max = buf.getFloat();
			rpm_rate_max = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		OP_REQUEST(0l),

		OP_SET(1l),

		OP_REPORT(2l);

		protected long value;

		OP(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static OP valueOf(long value) throws IllegalArgumentException {
			for (OP v : OP.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for OP: "+value);
		}
	}
}
