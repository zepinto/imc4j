package pt.lsts.imc.msg;

import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;

/**
 * The Heartbeat message is used to inform other modules that the
 * sending entity's system is running normally and communications
 * are alive.
 */
public class Heartbeat extends Message {
	public static final int ID_STATIC = 150;

	public int mgid() {
		return 150;
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
