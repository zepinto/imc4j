package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Request creating a new session with this remote peer. Example
 * session sequence is shown in the following diagram.
 * .. figure:: ../images/session_sequence.png
 * :align:  center
 */
public class CreateSession extends Message {
	public static final int ID_STATIC = 806;

	/**
	 * Session timeout, in seconds. If no messages are received from
	 * the remote peer, the session will be closed after this ammount
	 * of seconds have ellapsed.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long timeout = 0;

	public String abbrev() {
		return "CreateSession";
	}

	public int mgid() {
		return 806;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)timeout);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			timeout = buf.getInt() & 0xFFFFFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
