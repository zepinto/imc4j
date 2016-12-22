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
 * Formation controller paramenters, as: trajectory gains, control boundary layer thickness, and formation shape gains.
 */
public class FormCtrlParam extends Message {
	public static final int ID_STATIC = 820;

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
	public float LonGain = 0f;

	/**
	 * Trajectory gain over the vehicle lateral direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float LatGain = 0f;

	/**
	 * Control sliding surface boundary layer thickness.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long BondThick = 0;

	/**
	 * Formation shape gain (absolute vehicle position tracking).
	 * Leader control importance gain (relative to the sum of every other formation vehicle).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float LeadGain = 0f;

	/**
	 * Collision avoidance and formation shape gain (position tracking relative to the other formation vehicles).
	 * Individual vehicle importance gain (relative to the leader), when the relative position or the velocity state indicate higher probability of collision.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float DeconflGain = 0f;

	public String abbrev() {
		return "FormCtrlParam";
	}

	public int mgid() {
		return 820;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(Action != null? Action.value() : 0));
			_out.writeFloat(LonGain);
			_out.writeFloat(LatGain);
			_out.writeInt((int)BondThick);
			_out.writeFloat(LeadGain);
			_out.writeFloat(DeconflGain);
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
			LonGain = buf.getFloat();
			LatGain = buf.getFloat();
			BondThick = buf.getInt() & 0xFFFFFFFF;
			LeadGain = buf.getFloat();
			DeconflGain = buf.getFloat();
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
