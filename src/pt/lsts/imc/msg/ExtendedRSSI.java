package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.RSSIUnits;

/**
 * Measure of the RSSI by a networking device.
 * Indicates the gain or loss in the signal strenght due to the transmission
 * and reception equipment and the transmission medium and distance.
 */
public class ExtendedRSSI extends Message {
	public static final int ID_STATIC = 183;

	/**
	 * RSSI measurement.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float value = 0f;

	/**
	 * Indicates the units used for the RSSI value.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public RSSIUnits units = RSSIUnits.values()[0];

	public int mgid() {
		return 183;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(value);
			_out.writeByte((int)(units != null? units.value() : 0));
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
			units = RSSIUnits.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
