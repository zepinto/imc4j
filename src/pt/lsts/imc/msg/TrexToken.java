package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

public class TrexToken extends Message {
	public static final int ID_STATIC = 657;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String timeline = "";

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String predicate = "";

	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<TrexAttribute> attributes = new ArrayList<>();

	public String abbrev() {
		return "TrexToken";
	}

	public int mgid() {
		return 657;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, timeline);
			SerializationUtils.serializePlaintext(_out, predicate);
			SerializationUtils.serializeMsgList(_out, attributes);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			timeline = SerializationUtils.deserializePlaintext(buf);
			predicate = SerializationUtils.deserializePlaintext(buf);
			attributes = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
