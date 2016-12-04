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
 * The optical backscattering coefficient refers to all the photons that have been redirected in the backward directions
 * when a photon of light propagates in water and interacts with a "particle" (varying from water molecules to fish).
 */
public class OpticalBackscatter extends Message {
	public static final int ID_STATIC = 904;

	/**
	 * Optical Backscattering Coefficient.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "1/m"
	)
	public float value = 0f;

	public String abbrev() {
		return "OpticalBackscatter";
	}

	public int mgid() {
		return 904;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
