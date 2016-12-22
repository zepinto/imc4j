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
 * Waypoint coordinate of a Follow Trajectory maneuver.
 */
public class TrajectoryPoint extends Message {
	public static final int ID_STATIC = 464;

	/**
	 * The North offset of the North/East/Down coordinate of this
	 * point in relation to the trajectory start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * The East offset of the North/East/Down coordinate of this
	 * point in relation to the trajectory start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * The Down offset of the North/East/Down coordinate of this
	 * point in relation to the trajectory start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * The time offset relative to the previous trajectory point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float t = 0f;

	public String abbrev() {
		return "TrajectoryPoint";
	}

	public int mgid() {
		return 464;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			_out.writeFloat(t);
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
			t = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
