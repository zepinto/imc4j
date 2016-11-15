package pt.lsts.imc.msg;

import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;

/**
 * Request a list of known underwater acoustic systems. The
 * recipient of this message shall reply with an AcousticSystems
 * message.
 */
public class AcousticSystemsQuery extends Message {
	public static final int ID_STATIC = 212;

	public int mgid() {
		return 212;
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
