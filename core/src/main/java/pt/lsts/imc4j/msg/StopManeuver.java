package pt.lsts.imc4j.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Command used to stop currently executing maneuver.
 */
public class StopManeuver extends Message {
	public static final int ID_STATIC = 468;

	public String abbrev() {
		return "StopManeuver";
	}

	public int mgid() {
		return 468;
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
