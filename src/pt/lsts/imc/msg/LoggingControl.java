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
 * Control logging of messages to persistent storage.
 */
public class LoggingControl extends Message {
	public static final int ID_STATIC = 102;

	/**
	 * Operation to perform.
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
	public String name = "";

	public int mgid() {
		return 102;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			SerializationUtils.serializePlaintext(_out, name);
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
			name = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		COP_REQUEST_START(0l),

		COP_STARTED(1l),

		COP_REQUEST_STOP(2l),

		COP_STOPPED(3l),

		COP_REQUEST_CURRENT_NAME(4l),

		COP_CURRENT_NAME(5l);

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
