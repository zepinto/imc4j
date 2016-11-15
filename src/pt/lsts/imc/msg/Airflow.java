package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Airspeed along with airflow angles.
 */
public class Airflow extends Message {
	public static final int ID_STATIC = 363;

	/**
	 * Airspeed, the 2-norm of the relative velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float va = 0f;

	/**
	 * Angle of attack.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float aoa = 0f;

	/**
	 * Sideslip angle.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float ssa = 0f;

	public int mgid() {
		return 363;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(va);
			_out.writeFloat(aoa);
			_out.writeFloat(ssa);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			va = buf.getFloat();
			aoa = buf.getFloat();
			ssa = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
