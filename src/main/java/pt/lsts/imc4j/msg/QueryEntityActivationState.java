package pt.lsts.imc4j.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Query the activation/deactivation state of an entity. The
 * recipient shall reply with an EntityActivationState message.
 */
public class QueryEntityActivationState extends Message {
	public static final int ID_STATIC = 15;

	public String abbrev() {
		return "QueryEntityActivationState";
	}

	public int mgid() {
		return 15;
	}

	public byte[] serializeFields() {
		return new byte[0];
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
