package pt.lsts.imc4j.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Stops any executing actions and put the system in a standby mode.
 */
public class Abort extends Message {
	public static final int ID_STATIC = 550;

	public String abbrev() {
		return "Abort";
	}

	public int mgid() {
		return 550;
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
