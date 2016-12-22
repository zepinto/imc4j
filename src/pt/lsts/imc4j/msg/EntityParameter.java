package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Entity parameter.
 */
public class EntityParameter extends Message {
	public static final int ID_STATIC = 801;

	/**
	 * Name of the parameter.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	/**
	 * Current value of the parameter.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String value = "";

	public String abbrev() {
		return "EntityParameter";
	}

	public int mgid() {
		return 801;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			SerializationUtils.serializePlaintext(_out, value);
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
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
