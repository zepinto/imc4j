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
 * This message contains information, collected using USBL, about a
 * target's position.
 */
public class UsblPosition extends Message {
	public static final int ID_STATIC = 891;

	/**
	 * Target's IMC address.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int target = 0;

	/**
	 * X coordinate of the target in the local device's reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * Y coordinate of the target in the local device's reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * Z coordinate of the target in the local device's reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	public String abbrev() {
		return "UsblPosition";
	}

	public int mgid() {
		return 891;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(target);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			target = buf.getShort() & 0xFFFF;
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
