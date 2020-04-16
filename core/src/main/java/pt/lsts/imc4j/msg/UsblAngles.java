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
 * This message contains information, collected using USBL, about the
 * bearing and elevation of a target.
 */
public class UsblAngles extends Message {
	public static final int ID_STATIC = 890;

	/**
	 * Target's IMC address.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int target = 0;

	/**
	 * Target's bearing.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float bearing = 0f;

	/**
	 * Target's elevation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float elevation = 0f;

	public String abbrev() {
		return "UsblAngles";
	}

	public int mgid() {
		return 890;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(target);
			_out.writeFloat(bearing);
			_out.writeFloat(elevation);
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
			bearing = buf.getFloat();
			elevation = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
