package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Report of navigation data.
 * This is constituted by data which is not
 * part of the vehicle estimated state but
 * that the user may refer for more information.
 */
public class NavigationData extends Message {
	public static final int ID_STATIC = 355;

	/**
	 * The psi Euler angle bias from the vehicle's sensed attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float bias_psi = 0f;

	/**
	 * The angular velocity over body-fixed zz axis bias from sensor.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad/s"
	)
	public float bias_r = 0f;

	/**
	 * Course over ground given by NED ground velocity vectors.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float cog = 0f;

	/**
	 * Continuous psi Euler angle (without normalizations).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float cyaw = 0f;

	/**
	 * GPS rejection filter level.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float lbl_rej_level = 0f;

	/**
	 * LBL rejection filter level.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float gps_rej_level = 0f;

	/**
	 * Custom variable.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float custom_x = 0f;

	/**
	 * Custom variable.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float custom_y = 0f;

	/**
	 * Custom variable.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float custom_z = 0f;

	public int mgid() {
		return 355;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(bias_psi);
			_out.writeFloat(bias_r);
			_out.writeFloat(cog);
			_out.writeFloat(cyaw);
			_out.writeFloat(lbl_rej_level);
			_out.writeFloat(gps_rej_level);
			_out.writeFloat(custom_x);
			_out.writeFloat(custom_y);
			_out.writeFloat(custom_z);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			bias_psi = buf.getFloat();
			bias_r = buf.getFloat();
			cog = buf.getFloat();
			cyaw = buf.getFloat();
			lbl_rej_level = buf.getFloat();
			gps_rej_level = buf.getFloat();
			custom_x = buf.getFloat();
			custom_y = buf.getFloat();
			custom_z = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
