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
 * Acoustic operation.
 */
public class AcousticOperation extends Message {
	public static final int ID_STATIC = 211;

	/**
	 * Operation type.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * The meaning of this field depends on the operation and is
	 * explained in the operation's description.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String system = "";

	/**
	 * The meaning of this field depends on the operation and is
	 * explained in the operation's description.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float range = 0f;

	/**
	 * Argument for message send ('MSG') requests.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message msg = null;

	public String abbrev() {
		return "AcousticOperation";
	}

	public int mgid() {
		return 211;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			SerializationUtils.serializePlaintext(_out, system);
			_out.writeFloat(range);
			SerializationUtils.serializeInlineMsg(_out, msg);
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
			system = SerializationUtils.deserializePlaintext(buf);
			range = buf.getFloat();
			msg = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		AOP_ABORT(0l),

		AOP_ABORT_IP(1l),

		AOP_ABORT_TIMEOUT(2l),

		AOP_ABORT_ACKED(3l),

		AOP_RANGE(4l),

		AOP_RANGE_IP(5l),

		AOP_RANGE_TIMEOUT(6l),

		AOP_RANGE_RECVED(7l),

		AOP_BUSY(8l),

		AOP_UNSUPPORTED(9l),

		AOP_NO_TXD(10l),

		AOP_MSG(11l),

		AOP_MSG_QUEUED(12l),

		AOP_MSG_IP(13l),

		AOP_MSG_DONE(14l),

		AOP_MSG_FAILURE(15l),

		AOP_MSG_SHORT(16l);

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
