package pt.lsts.imc.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Request entities to report their state. Entities should respond
 * by issuing an appropriate EntityState message.
 */
public class QueryEntityState extends Message {
	public static final int ID_STATIC = 2;

	public String abbrev() {
		return "QueryEntityState";
	}

	public int mgid() {
		return 2;
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
