package pt.lsts.imc4j.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Notification of completion of a Teleoperation maneuver.
 */
public class TeleoperationDone extends Message {
	public static final int ID_STATIC = 460;

	public String abbrev() {
		return "TeleoperationDone";
	}

	public int mgid() {
		return 460;
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
