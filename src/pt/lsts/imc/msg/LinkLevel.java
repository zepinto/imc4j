package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Measurement of link level quality. For instance, this may
 * correspond to the acknowledgment ratio of a link. But,
 * generally, the measure is link-dependent.
 */
public class LinkLevel extends Message {
	public static final int ID_STATIC = 155;

	/**
	 * Link level value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float value = 0f;

	public int mgid() {
		return 155;
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
