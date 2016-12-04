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
 * Formation control performance evaluation variables.
 */
public class FormationEval extends Message {
	public static final int ID_STATIC = 821;

	/**
	 * Mean position error relative to the formation reference.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float err_mean = 0f;

	/**
	 * Overall minimum distance to any other vehicle in the formation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float dist_min_abs = 0f;

	/**
	 * Mean minimum distance to any other vehicle in the formation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float dist_min_mean = 0f;

	public String abbrev() {
		return "FormationEval";
	}

	public int mgid() {
		return 821;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(err_mean);
			_out.writeFloat(dist_min_abs);
			_out.writeFloat(dist_min_mean);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			err_mean = buf.getFloat();
			dist_min_abs = buf.getFloat();
			dist_min_mean = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
