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
 * This message contains information, collected using USBL, about the
 * bearing and elevation of a target.
 */
public class UsblAnglesExtended extends Message {
	public static final int ID_STATIC = 898;

	/**
	 * Target's system name.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String target = "";

	/**
	 * Target's bearing in the local device's reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float lbearing = 0f;

	/**
	 * Target's elevation in the local device's reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float lelevation = 0f;

	/**
	 * Target's bearing in the navigation reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float bearing = 0f;

	/**
	 * Target's elevation in the navigation reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float elevation = 0f;

	/**
	 * Rotation around the device longitudinal axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public float phi = 0f;

	/**
	 * Rotation around the device lateral or transverse axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 1.57079632679490,
			min = -1.57079632679490,
			units = "rad"
	)
	public float theta = 0f;

	/**
	 * Rotation around the device vertical axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public float psi = 0f;

	/**
	 * Accuracy of the fix.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = 0,
			units = "rad"
	)
	public float accuracy = 0f;

	public String abbrev() {
		return "UsblAnglesExtended";
	}

	public int mgid() {
		return 898;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, target);
			_out.writeFloat(lbearing);
			_out.writeFloat(lelevation);
			_out.writeFloat(bearing);
			_out.writeFloat(elevation);
			_out.writeFloat(phi);
			_out.writeFloat(theta);
			_out.writeFloat(psi);
			_out.writeFloat(accuracy);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			target = SerializationUtils.deserializePlaintext(buf);
			lbearing = buf.getFloat();
			lelevation = buf.getFloat();
			bearing = buf.getFloat();
			elevation = buf.getFloat();
			phi = buf.getFloat();
			theta = buf.getFloat();
			psi = buf.getFloat();
			accuracy = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
