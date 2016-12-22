package pt.lsts.imc4j.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Command to obtain the operational limits in use by the vehicle.
 */
public class GetOperationalLimits extends Message {
	public static final int ID_STATIC = 505;

	public String abbrev() {
		return "GetOperationalLimits";
	}

	public int mgid() {
		return 505;
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
