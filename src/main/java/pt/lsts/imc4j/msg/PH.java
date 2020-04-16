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
 * Report of pH.
 */
public class PH extends Message {
	public static final int ID_STATIC = 298;

	/**
	 * The value of the pH as measured by the sensor.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float value = 0f;

	public String abbrev() {
		return "PH";
	}

	public int mgid() {
		return 298;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
