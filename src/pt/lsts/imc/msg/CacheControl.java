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
 * Control caching of messages to persistent storage.
 */
public class CacheControl extends Message {
	public static final int ID_STATIC = 101;

	/**
	 * Operation to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Destination for the cache snapshot file.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String snapshot = "";

	/**
	 * Message to store.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message message = null;

	public int mgid() {
		return 101;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			SerializationUtils.serializePlaintext(_out, snapshot);
			SerializationUtils.serializeInlineMsg(_out, message);
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
			snapshot = SerializationUtils.deserializePlaintext(buf);
			message = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		COP_STORE(0l),

		COP_LOAD(1l),

		COP_CLEAR(2l),

		COP_COPY(3l),

		COP_COPY_COMPLETE(4l);

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
