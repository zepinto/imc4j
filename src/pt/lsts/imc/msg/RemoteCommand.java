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
 * Command to remote system. If a system receives a RemoteCommand and it isn't the intended recipient, then it should
 * resend it.
 */
public class RemoteCommand extends RemoteData {
	public static final int ID_STATIC = 188;

	/**
	 * IMC id of the original sender.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int original_source = 0;

	/**
	 * IMC id of the recipient.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int destination = 0;

	/**
	 * Expiration time of the message (Epoch Time), in seconds. If the message doesn't reach the destination within timeout,
	 * the validity of the message expires and there will be no more attempts to transmit the message.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double timeout = 0;

	/**
	 * Command to be unpacked by the recipient.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message cmd = null;

	public String abbrev() {
		return "RemoteCommand";
	}

	public int mgid() {
		return 188;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(original_source);
			_out.writeShort(destination);
			_out.writeDouble(timeout);
			SerializationUtils.serializeInlineMsg(_out, cmd);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			original_source = buf.getShort() & 0xFFFF;
			destination = buf.getShort() & 0xFFFF;
			timeout = buf.getDouble();
			cmd = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
