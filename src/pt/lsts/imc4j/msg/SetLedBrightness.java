package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Control the brightness of an LED (Light-Emitting Diode). The
 * recipient of this message shall set the intensity of the LED to
 * the desired 'value' and reply with 'LedBrightness'.
 */
public class SetLedBrightness extends Message {
	public static final int ID_STATIC = 314;

	/**
	 * LED name.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	/**
	 * Desired brightness value.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int value = 0;

	public String abbrev() {
		return "SetLedBrightness";
	}

	public int mgid() {
		return 314;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			_out.writeByte(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			name = SerializationUtils.deserializePlaintext(buf);
			value = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
