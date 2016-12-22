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
 * Report of software CPU usage.
 */
public class CpuUsage extends Message {
	public static final int ID_STATIC = 7;

	/**
	 * The CPU usage, in percentage, of the sending software.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 100,
			units = "%"
	)
	public int value = 0;

	public String abbrev() {
		return "CpuUsage";
	}

	public int mgid() {
		return 7;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
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
			value = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
