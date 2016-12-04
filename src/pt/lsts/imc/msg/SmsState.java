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

public class SmsState extends Message {
	public static final int ID_STATIC = 159;

	/**
	 * Sequence number.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long seq = 0;

	/**
	 * Current state of an SMS transaction.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String error = "";

	public String abbrev() {
		return "SmsState";
	}

	public int mgid() {
		return 159;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)seq);
			_out.writeByte((int)(state != null? state.value() : 0));
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
			seq = buf.getInt() & 0xFFFFFFFF;
			state = STATE.valueOf(buf.get() & 0xFF);
			error = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		SMS_ACCEPTED(0l),

		SMS_REJECTED(1l),

		SMS_INTERRUPTED(2l),

		SMS_COMPLETED(3l),

		SMS_IDLE(4l),

		SMS_TRANSMITTING(5l),

		SMS_RECEIVING(6l);

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
