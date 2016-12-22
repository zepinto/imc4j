package pt.lsts.imc4j.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Request the state of power channels.
 */
public class QueryPowerChannelState extends Message {
	public static final int ID_STATIC = 310;

	public String abbrev() {
		return "QueryPowerChannelState";
	}

	public int mgid() {
		return 310;
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
