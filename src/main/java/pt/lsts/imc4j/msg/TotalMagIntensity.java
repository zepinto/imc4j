package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

public class TotalMagIntensity extends Message {
	public static final int ID_STATIC = 2006;

	/**
	 * Total Magnetic Field Intensity (TMI)
	 */
	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double value = 0;

	public String abbrev() {
		return "TotalMagIntensity";
	}

	public int mgid() {
		return 2006;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
