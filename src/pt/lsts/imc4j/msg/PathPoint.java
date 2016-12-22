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
 * Waypoint coordinate of a Follow Path maneuver.
 */
public class PathPoint extends Message {
	public static final int ID_STATIC = 458;

	/**
	 * The North offset of the North/East/Down coordinate of this
	 * point in relation to the path start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * The East offset of the North/East/Down coordinate of this
	 * point in relation to the path start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * The Down offset of the North/East/Down coordinate of this
	 * point in relation to the path start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	public String abbrev() {
		return "PathPoint";
	}

	public int mgid() {
		return 458;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
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
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
