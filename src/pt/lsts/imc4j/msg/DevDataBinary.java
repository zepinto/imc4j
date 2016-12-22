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
 * Verbatim representation of device data in binary format.
 */
public class DevDataBinary extends Message {
	public static final int ID_STATIC = 274;

	/**
	 * Raw binary data as extracted directly from the device.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] value = new byte[0];

	public String abbrev() {
		return "DevDataBinary";
	}

	public int mgid() {
		return 274;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializeRawdata(_out, value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
