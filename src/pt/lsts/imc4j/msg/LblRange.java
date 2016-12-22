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
 * When the vehicle uses Long Base Line navigation, this message
 * notifies that a new range was received from one of the acoustics
 * transponders. The message fields are used to identify the range
 * value and the transponder name.
 */
public class LblRange extends Message {
	public static final int ID_STATIC = 200;

	/**
	 * Identification number of the acoustic transponder from which
	 * the range information was received.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int id = 0;

	/**
	 * Distance to the acoustic transponder.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float range = 0f;

	public String abbrev() {
		return "LblRange";
	}

	public int mgid() {
		return 200;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(id);
			_out.writeFloat(range);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			id = buf.get() & 0xFF;
			range = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
