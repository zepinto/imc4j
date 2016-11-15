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
 * A plan variable.
 */
public class PlanVariable extends Message {
	public static final int ID_STATIC = 561;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String value = "";

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ACCESS access = ACCESS.values()[0];

	public int mgid() {
		return 561;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			SerializationUtils.serializePlaintext(_out, value);
			_out.writeByte((int)type.value());
			_out.writeByte((int)access.value());
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
			value = SerializationUtils.deserializePlaintext(buf);
			type = TYPE.valueOf(buf.get() & 0xFF);
			access = ACCESS.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		PVT_BOOLEAN(0l),

		PVT_NUMBER(1l),

		PVT_TEXT(2l),

		PVT_MESSAGE(3l);

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

	public enum ACCESS {
		PVA_INPUT(0l),

		PVA_OUTPUT(1l),

		PVA_LOCAL(2l);

		protected long value;

		ACCESS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ACCESS valueOf(long value) throws IllegalArgumentException {
			for (ACCESS v : ACCESS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ACCESS: "+value);
		}
	}
}
