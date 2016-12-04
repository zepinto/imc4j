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
 * Beam configuration of the device.
 */
public class BeamConfig extends Message {
	public static final int ID_STATIC = 283;

	/**
	 * Beam width of the instrument. A negative number denotes that
	 * this information is not available or is not applicable.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = 0,
			units = "rad"
	)
	public float beam_width = 0f;

	/**
	 * Beam height of the instrument. A negative number denotes that
	 * this information is not available or is not applicable.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = 0,
			units = "rad"
	)
	public float beam_height = 0f;

	public String abbrev() {
		return "BeamConfig";
	}

	public int mgid() {
		return 283;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(beam_width);
			_out.writeFloat(beam_height);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			beam_width = buf.getFloat();
			beam_height = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
