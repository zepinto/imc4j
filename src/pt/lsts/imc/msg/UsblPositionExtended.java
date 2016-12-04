package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * This message contains information, collected using USBL, about a
 * target's position.
 */
public class UsblPositionExtended extends Message {
	public static final int ID_STATIC = 899;

	/**
	 * Target's system name.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String target = "";

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

	/**
	 * X coordinate of the target in the navigation reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float n = 0f;

	/**
	 * Y coordinate of the target in the navigation reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float e = 0f;

	/**
	 * Z coordinate of the target in the navigation reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float d = 0f;

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
	 * Accuracy of the position fix.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float accuracy = 0f;

	public String abbrev() {
		return "UsblPositionExtended";
	}

	public int mgid() {
		return 899;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, target);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			_out.writeFloat(n);
			_out.writeFloat(e);
			_out.writeFloat(d);
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
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
			n = buf.getFloat();
			e = buf.getFloat();
			d = buf.getFloat();
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
