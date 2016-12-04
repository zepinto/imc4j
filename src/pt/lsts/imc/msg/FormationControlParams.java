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

/**
 * Formation controller paramenters, as: trajectory gains,
 * control boundary layer thickness, and formation shape gains.
 */
public class FormationControlParams extends Message {
	public static final int ID_STATIC = 822;

	/**
	 * Action on the vehicle formation control parameters.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ACTION Action = ACTION.values()[0];

	/**
	 * Trajectory gain over the vehicle longitudinal direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float lon_gain = 0f;

	/**
	 * Trajectory gain over the vehicle lateral direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float lat_gain = 0f;

	/**
	 * Control sliding surface boundary layer thickness.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float bond_thick = 0f;

	/**
	 * Formation shape gain (absolute vehicle position tracking).
	 * Leader control importance gain (relative to the sum of every other formation vehicle).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float lead_gain = 0f;

	/**
	 * Collision avoidance and formation shape gain (position tracking relative to the other formation vehicles).
	 * Individual vehicle importance gain (relative to the leader), when the relative position or the velocity state indicate higher probability of collision.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float deconfl_gain = 0f;

	/**
	 * Switch gain to compensate the worst case of the wind flow acceleration.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float accel_switch_gain = 0f;

	/**
	 * Inter-vehicle safety distance.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float safe_dist = 0f;

	/**
	 * Distance offset which defines the buffer area beyond the safety distace.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float deconflict_offset = 0f;

	/**
	 * Safety margin to compensate for possible shortfalls from the predicted maximum acceleration that a vehicle can generate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float accel_safe_margin = 0f;

	/**
	 * Maximum predicted longitudinal acceleration a vehicle can generate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float accel_lim_x = 0f;

	public String abbrev() {
		return "FormationControlParams";
	}

	public int mgid() {
		return 822;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(Action != null? Action.value() : 0));
			_out.writeFloat(lon_gain);
			_out.writeFloat(lat_gain);
			_out.writeFloat(bond_thick);
			_out.writeFloat(lead_gain);
			_out.writeFloat(deconfl_gain);
			_out.writeFloat(accel_switch_gain);
			_out.writeFloat(safe_dist);
			_out.writeFloat(deconflict_offset);
			_out.writeFloat(accel_safe_margin);
			_out.writeFloat(accel_lim_x);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			Action = ACTION.valueOf(buf.get() & 0xFF);
			lon_gain = buf.getFloat();
			lat_gain = buf.getFloat();
			bond_thick = buf.getFloat();
			lead_gain = buf.getFloat();
			deconfl_gain = buf.getFloat();
			accel_switch_gain = buf.getFloat();
			safe_dist = buf.getFloat();
			deconflict_offset = buf.getFloat();
			accel_safe_margin = buf.getFloat();
			accel_lim_x = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ACTION {
		OP_REQ(0l),

		OP_SET(1l),

		OP_REP(2l);

		protected long value;

		ACTION(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ACTION valueOf(long value) throws IllegalArgumentException {
			for (ACTION v : ACTION.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ACTION: "+value);
		}
	}
}
