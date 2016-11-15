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
 * State of entity activation/deactivation.
 */
public class EntityActivationState extends Message {
	public static final int ID_STATIC = 14;

	/**
	 * Current state.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	/**
	 * Human-readable error message.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String error = "";

	public int mgid() {
		return 14;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)state.value());
			SerializationUtils.serializePlaintext(_out, error);
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
			error = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		EAS_INACTIVE(0l),

		EAS_ACTIVE(1l),

		EAS_ACT_IP(2l),

		EAS_ACT_DONE(3l),

		EAS_ACT_FAIL(4l),

		EAS_DEACT_IP(5l),

		EAS_DEACT_DONE(6l),

		EAS_DEACT_FAIL(7l);

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
}
