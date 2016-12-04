package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Report of navigation uncertainty.
 * This is usually given by the output of the state
 * covariance matrix of an Extended Kalman Filter.
 */
public class NavigationUncertainty extends Message {
	public static final int ID_STATIC = 354;

	/**
	 * The North offset variance of the North/East/Down
	 * field with respect to LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * The East offset variance of the North/East/Down
	 * field with respect to LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * The Down offset variance of the North/East/Down
	 * field with respect to LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * The phi Euler angle variance from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float phi = 0f;

	/**
	 * The theta Euler angle variance from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float theta = 0f;

	/**
	 * The psi Euler angle variance from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float psi = 0f;

	/**
	 * The angular velocity variance over body-fixed xx axis (roll).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad/s"
	)
	public float p = 0f;

	/**
	 * The angular velocity variance over body-fixed yy axis (pitch).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad/s"
	)
	public float q = 0f;

	/**
	 * The angular velocity variance over body-fixed zz axis (yaw).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad/s"
	)
	public float r = 0f;

	/**
	 * Body-fixed frame xx axis velocity variance component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float u = 0f;

	/**
	 * Body-fixed frame yy axis velocity variance component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float v = 0f;

	/**
	 * Body-fixed frame zz axis velocity variance component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float w = 0f;

	/**
	 * The psi Euler angle bias variance from the vehicle's sensed attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float bias_psi = 0f;

	/**
	 * The angular velocity over body-fixed zz axis bias variance from sensor.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad/s"
	)
	public float bias_r = 0f;

	public String abbrev() {
		return "NavigationUncertainty";
	}

	public int mgid() {
		return 354;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			_out.writeFloat(phi);
			_out.writeFloat(theta);
			_out.writeFloat(psi);
			_out.writeFloat(p);
			_out.writeFloat(q);
			_out.writeFloat(r);
			_out.writeFloat(u);
			_out.writeFloat(v);
			_out.writeFloat(w);
			_out.writeFloat(bias_psi);
			_out.writeFloat(bias_r);
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
			phi = buf.getFloat();
			theta = buf.getFloat();
			psi = buf.getFloat();
			p = buf.getFloat();
			q = buf.getFloat();
			r = buf.getFloat();
			u = buf.getFloat();
			v = buf.getFloat();
			w = buf.getFloat();
			bias_psi = buf.getFloat();
			bias_r = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
