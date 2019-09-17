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
 * Reply sent in response to a Acoustic Text sending request.
 */
public class AcousticStatus extends Message {
	public static final int ID_STATIC = 216;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATUS status = STATUS.values()[0];

	/**
	 * Status description.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String info = "";

	/**
	 * The meaning of this field depends on the operation and is
	 * explained in the operation's description.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float range = 0f;

	public String abbrev() {
		return "AcousticStatus";
	}

	public int mgid() {
		return 216;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeByte((int)(status != null? status.value() : 0));
			SerializationUtils.serializePlaintext(_out, info);
			_out.writeFloat(range);
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
			type = TYPE.valueOf(buf.get() & 0xFF);
			status = STATUS.valueOf(buf.get() & 0xFF);
			info = SerializationUtils.deserializePlaintext(buf);
			range = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		TYPE_ABORT(0l),

		TYPE_RANGE(1l),

		TYPE_REVERSE_RANGE(2l),

		TYPE_MSG(3l),

		TYPE_RAW(4l);

		protected long value;

		TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static TYPE valueOf(long value) throws IllegalArgumentException {
			for (TYPE v : TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for TYPE: "+value);
		}
	}

	public enum STATUS {
		STATUS_QUEUED(0l),

		STATUS_IN_PROGRESS(1l),

		STATUS_SENT(2l),

		STATUS_RANGE_RECEIVED(3l),

		STATUS_BUSY(100l),

		STATUS_INPUT_FAILURE(101l),

		STATUS_ERROR(102l),

		STATUS_UNSUPPORTED(666l);

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
