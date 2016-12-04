package pt.lsts.imc.msg;

import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;

/**
 * Request the destination system to send its parameters XML file via a
 * :ref:`ParametersXml` message.
 */
public class GetParametersXml extends Message {
	public static final int ID_STATIC = 894;

	public String abbrev() {
		return "GetParametersXml";
	}

	public int mgid() {
		return 894;
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
