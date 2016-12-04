package pt.lsts.imc.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * This message signals that an :ref:`Abort` message was received and acted upon.
 */
public class Aborted extends Message {
	public static final int ID_STATIC = 889;

	public String abbrev() {
		return "Aborted";
	}

	public int mgid() {
		return 889;
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
