package pt.lsts.imc.msg;

import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;

/**
 * A "ImageTracking" is a maneuver specifying a particular heading to the
 * detected object.
 */
public class ImageTracking extends Maneuver {
	public static final int ID_STATIC = 490;

	public int mgid() {
		return 490;
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
