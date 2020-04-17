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
 * Request SMS Text sending.
 */
public class SmsRequest extends Message {
	public static final int ID_STATIC = 517;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	/**
	 * Recipient identifier (number or name).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String destination = "";

	/**
	 * Period of time to send message (in seconds).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double timeout = 0;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sms_text = "";

	public String abbrev() {
		return "SmsRequest";
	}

	public int mgid() {
		return 517;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			SerializationUtils.serializePlaintext(_out, destination);
			_out.writeDouble(timeout);
			SerializationUtils.serializePlaintext(_out, sms_text);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			req_id = buf.getShort() & 0xFFFF;
			destination = SerializationUtils.deserializePlaintext(buf);
			timeout = buf.getDouble();
			sms_text = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
