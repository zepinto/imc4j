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
 * Report of storage usage.
 */
public class StorageUsage extends Message {
	public static final int ID_STATIC = 100;

	/**
	 * The available storage of the reporting device.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "MiB"
	)
	public long available = 0;

	/**
	 * The percentage of storage used by the reporting device.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 100,
			units = "%"
	)
	public int value = 0;

	public String abbrev() {
		return "StorageUsage";
	}

	public int mgid() {
		return 100;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)available);
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
			available = buf.getInt() & 0xFFFFFFFF;
			value = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
