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
 * Message generated when tasks bind to messages.
 */
public class TransportBindings extends Message {
	public static final int ID_STATIC = 8;

	/**
	 * The name of the consumer (e.g. task name).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String consumer = "";

	/**
	 * The id of the message to be listened to.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int message_id = 0;

	public int mgid() {
		return 8;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, consumer);
			_out.writeShort(message_id);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			consumer = SerializationUtils.deserializePlaintext(buf);
			message_id = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
