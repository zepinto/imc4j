package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

public class SetEntityParameters extends Message {
	public static final int ID_STATIC = 804;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<EntityParameter> params = new ArrayList<>();

	public String abbrev() {
		return "SetEntityParameters";
	}

	public int mgid() {
		return 804;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			SerializationUtils.serializeMsgList(_out, params);
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
			params = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
