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
 * Vehicle dynamics parameters for 3DOF, 4DOF or 5DOF simulations.
 */
public class DynamicsSimParam extends Message {
	public static final int ID_STATIC = 53;

	/**
	 * Action on the vehicle simulation parameters for the formation control
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Proportional gain from the TAS (True Airspeed) error to the longitudinal acceleration.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float tas2acc_pgain = 0f;

	/**
	 * Proportional gain from the bank angle error to the bank angular rate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float bank2p_pgain = 0f;

	public int mgid() {
		return 53;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)op.value());
			_out.writeFloat(tas2acc_pgain);
			_out.writeFloat(bank2p_pgain);
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
			tas2acc_pgain = buf.getFloat();
			bank2p_pgain = buf.getFloat();
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
