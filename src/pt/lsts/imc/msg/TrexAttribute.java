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

public class TrexAttribute extends Message {
	public static final int ID_STATIC = 656;

	/**
	 * Name of this attribute.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ATTR_TYPE attr_type = ATTR_TYPE.values()[0];

	/**
	 * Lower bound of this interval. Empty text means no bound.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String min = "";

	/**
	 * Upper bound of this interval. Empty text means no bound.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String max = "";

	public String abbrev() {
		return "TrexAttribute";
	}

	public int mgid() {
		return 656;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			_out.writeByte((int)(attr_type != null? attr_type.value() : 0));
			SerializationUtils.serializePlaintext(_out, min);
			SerializationUtils.serializePlaintext(_out, max);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			name = SerializationUtils.deserializePlaintext(buf);
			attr_type = ATTR_TYPE.valueOf(buf.get() & 0xFF);
			min = SerializationUtils.deserializePlaintext(buf);
			max = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ATTR_TYPE {
		TYPE_BOOL(1l),

		TYPE_INT(2l),

		TYPE_FLOAT(3l),

		TYPE_STRING(4l),

		TYPE_ENUM(5l);

		protected long value;

		ATTR_TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ATTR_TYPE valueOf(long value) throws IllegalArgumentException {
			for (ATTR_TYPE v : ATTR_TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ATTR_TYPE: "+value);
		}
	}
}
