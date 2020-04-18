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
 * Samples to calculate a vertical profile.
 */
public class ProfileSample extends Message {
	public static final int ID_STATIC = 112;

	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "dm"
	)
	public int depth = 0;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float avg = 0f;

	public String abbrev() {
		return "ProfileSample";
	}

	public int mgid() {
		return 112;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(depth);
			_out.writeFloat(avg);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			depth = buf.getShort() & 0xFFFF;
			avg = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
