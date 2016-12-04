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

public class TrexPlan extends Message {
	public static final int ID_STATIC = 658;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String reactor = "";

	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<TrexToken> tokens = new ArrayList<>();

	public String abbrev() {
		return "TrexPlan";
	}

	public int mgid() {
		return 658;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, reactor);
			SerializationUtils.serializeMsgList(_out, tokens);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			reactor = SerializationUtils.deserializePlaintext(buf);
			tokens = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
