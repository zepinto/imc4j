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
 * Request an acoustic modem driver to measure the distance to another system.
 */
public class UamTxRange extends Message {
	public static final int ID_STATIC = 818;

	/**
	 * A sequence identifier that should be incremented for each
	 * request. This number will then be used to issue transmission
	 * status updates via the message UamTxStatus.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int seq = 0;

	/**
	 * The canonical name of the target system.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_dst = "";

	/**
	 * Maximum amount of time to wait for a reply.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float timeout = 0f;

	public String abbrev() {
		return "UamTxRange";
	}

	public int mgid() {
		return 818;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(seq);
			SerializationUtils.serializePlaintext(_out, sys_dst);
			_out.writeFloat(timeout);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			seq = buf.getShort() & 0xFFFF;
			sys_dst = SerializationUtils.deserializePlaintext(buf);
			timeout = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
