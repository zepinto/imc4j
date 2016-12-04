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
import pt.lsts.imc.util.SerializationUtils;

/**
 * State of plan control.
 */
public class PlanControlState extends Message {
	public static final int ID_STATIC = 560;

	/**
	 * Describes overall state.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	/**
	 * Identifier of plan currently loaded.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	/**
	 * Current plan estimated time to completion.
	 * The value will be -1 if the time is unknown or undefined.
	 */
	@FieldType(
			type = IMCField.TYPE_INT32,
			units = "s"
	)
	public int plan_eta = 0;

	/**
	 * Current plan estimated progress in percent.
	 * The value will be negative if unknown or undefined.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "%"
	)
	public float plan_progress = 0f;

	/**
	 * Current node ID, when executing a plan.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String man_id = "";

	/**
	 * Type of maneuver being executed (IMC serialization id),
	 * when executing a plan.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int man_type = 0;

	/**
	 * Current node estimated time to completion, when executing a plan.
	 * The value will be -1 if the time is unknown or undefined.
	 */
	@FieldType(
			type = IMCField.TYPE_INT32,
			units = "s"
	)
	public int man_eta = 0;

	/**
	 * Outcome of the last executed plan.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public LAST_OUTCOME last_outcome = LAST_OUTCOME.values()[0];

	public String abbrev() {
		return "PlanControlState";
	}

	public int mgid() {
		return 560;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(state != null? state.value() : 0));
			SerializationUtils.serializePlaintext(_out, plan_id);
			_out.writeInt((int)plan_eta);
			_out.writeFloat(plan_progress);
			SerializationUtils.serializePlaintext(_out, man_id);
			_out.writeShort(man_type);
			_out.writeInt((int)man_eta);
			_out.writeByte((int)(last_outcome != null? last_outcome.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			state = STATE.valueOf(buf.get() & 0xFF);
			plan_id = SerializationUtils.deserializePlaintext(buf);
			plan_eta = buf.getInt();
			plan_progress = buf.getFloat();
			man_id = SerializationUtils.deserializePlaintext(buf);
			man_type = buf.getShort() & 0xFFFF;
			man_eta = buf.getInt();
			last_outcome = LAST_OUTCOME.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		PCS_BLOCKED(0l),

		PCS_READY(1l),

		PCS_INITIALIZING(2l),

		PCS_EXECUTING(3l);

		protected long value;

		STATE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATE valueOf(long value) throws IllegalArgumentException {
			for (STATE v : STATE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATE: "+value);
		}
	}

	public enum LAST_OUTCOME {
		LPO_NONE(0l),

		LPO_SUCCESS(1l),

		LPO_FAILURE(2l);

		protected long value;

		LAST_OUTCOME(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static LAST_OUTCOME valueOf(long value) throws IllegalArgumentException {
			for (LAST_OUTCOME v : LAST_OUTCOME.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for LAST_OUTCOME: "+value);
		}
	}
}
