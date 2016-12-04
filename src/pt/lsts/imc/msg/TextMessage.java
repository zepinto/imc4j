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

/**
 * A text message has been received.
 */
public class TextMessage extends Message {
	public static final int ID_STATIC = 160;

	/**
	 * Message origin (if known).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String origin = "";

	/**
	 * Message contents.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String text = "";

	public String abbrev() {
		return "TextMessage";
	}

	public int mgid() {
		return 160;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, origin);
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
			origin = SerializationUtils.deserializePlaintext(buf);
			text = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
