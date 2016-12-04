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
 * This message is sent in reply to an AcousticSystemsQuery message
 * and lists all known underwater acoustic systems (modems, narrow
 * band transponders, etc).
 */
public class AcousticSystems extends Message {
	public static final int ID_STATIC = 213;

	/**
	 * Comma separated list of known acoustic system names.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "List"
	)
	public String list = "";

	public String abbrev() {
		return "AcousticSystems";
	}

	public int mgid() {
		return 213;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, list);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			list = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
