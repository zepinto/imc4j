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
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Reply sent in response to a SMS sending request.
 */
public class SmsStatus extends Message {
	public static final int ID_STATIC = 518;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATUS status = STATUS.values()[0];

	/**
	 * Error description.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String info = "";

	public String abbrev() {
		return "SmsStatus";
	}

	public int mgid() {
		return 518;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			_out.writeByte((int)(status != null? status.value() : 0));
			SerializationUtils.serializePlaintext(_out, info);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			req_id = buf.getShort() & 0xFFFF;
			status = STATUS.valueOf(buf.get() & 0xFF);
			info = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATUS {
		SMSSTAT_QUEUED(0l),

		SMSSTAT_SENT(1l),

		SMSSTAT_INPUT_FAILURE(101l),

		SMSSTAT_ERROR(102l);

		protected long value;

		STATUS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATUS valueOf(long value) throws IllegalArgumentException {
			for (STATUS v : STATUS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATUS: "+value);
		}
	}
}
