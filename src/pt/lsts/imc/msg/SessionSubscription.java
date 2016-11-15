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

public class SessionSubscription extends Message {
	public static final int ID_STATIC = 808;

	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long sessid = 0;

	/**
	 * Comma-separated list of messages to subscribe. Example:
	 * "EstimatedState,EulerAngles,Temperature"
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String messages = "";

	public int mgid() {
		return 808;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)sessid);
			SerializationUtils.serializePlaintext(_out, messages);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			sessid = buf.getInt() & 0xFFFFFFFF;
			messages = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
