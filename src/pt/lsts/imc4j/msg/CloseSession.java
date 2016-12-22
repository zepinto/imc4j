package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

/**
 * Request closing of an ongoing session
 */
public class CloseSession extends Message {
	public static final int ID_STATIC = 807;

	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long sessid = 0;

	public String abbrev() {
		return "CloseSession";
	}

	public int mgid() {
		return 807;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)sessid);
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
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
