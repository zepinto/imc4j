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
 * Send an acoustic message.
 */
public class AcousticMessage extends Message {
	public static final int ID_STATIC = 206;

	/**
	 * Message to send.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message message = null;

	public String abbrev() {
		return "AcousticMessage";
	}

	public int mgid() {
		return 206;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
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
			message = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
