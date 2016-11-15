package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Measure of the RSSI by a networking device.
 * Indicates the gain or loss in the signal strength due to the transmission and reception equipment and the transmission medium and distance.
 */
public class RSSI extends Message {
	public static final int ID_STATIC = 153;

	/**
	 * RSSI measurement.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 100,
			units = "%"
	)
	public float value = 0f;

	public int mgid() {
		return 153;
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
