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
 * Send a SMS message.
 */
public class Sms extends Message {
	public static final int ID_STATIC = 156;

	/**
	 * Target mobile device number.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String number = "";

	/**
	 * Timeout for sending message.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int timeout = 0;

	/**
	 * Message contents.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String contents = "";

	public int mgid() {
		return 156;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, number);
			_out.writeShort(timeout);
			SerializationUtils.serializePlaintext(_out, contents);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			number = SerializationUtils.deserializePlaintext(buf);
			timeout = buf.getShort() & 0xFFFF;
			contents = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
