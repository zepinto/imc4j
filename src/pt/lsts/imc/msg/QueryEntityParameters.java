package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

public class QueryEntityParameters extends Message {
	public static final int ID_STATIC = 803;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String visibility = "";

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String scope = "";

	public String abbrev() {
		return "QueryEntityParameters";
	}

	public int mgid() {
		return 803;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			SerializationUtils.serializePlaintext(_out, visibility);
			SerializationUtils.serializePlaintext(_out, scope);
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
			visibility = SerializationUtils.deserializePlaintext(buf);
			scope = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
