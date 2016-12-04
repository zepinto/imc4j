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
 * Human readable message reporting an event of interest.
 */
public class LogBookEntry extends Message {
	public static final int ID_STATIC = 103;

	/**
	 * Type of message.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Timestamp (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double htime = 0;

	/**
	 * Message context.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String context = "";

	/**
	 * Message text.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String text = "";

	public String abbrev() {
		return "LogBookEntry";
	}

	public int mgid() {
		return 103;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeDouble(htime);
			SerializationUtils.serializePlaintext(_out, context);
			SerializationUtils.serializePlaintext(_out, text);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			type = TYPE.valueOf(buf.get() & 0xFF);
			htime = buf.getDouble();
			context = SerializationUtils.deserializePlaintext(buf);
			text = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		LBET_INFO(0l),

		LBET_WARNING(1l),

		LBET_ERROR(2l),

		LBET_CRITICAL(3l),

		LBET_DEBUG(4l);

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
}
