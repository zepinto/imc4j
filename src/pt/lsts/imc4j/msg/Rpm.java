package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

/**
 * Number of revolutions per minute.
 */
public class Rpm extends Message {
	public static final int ID_STATIC = 250;

	/**
	 * Number of revolutions per minute.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "rpm"
	)
	public int value = 0;

	public String abbrev() {
		return "Rpm";
	}

	public int mgid() {
		return 250;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getShort();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
