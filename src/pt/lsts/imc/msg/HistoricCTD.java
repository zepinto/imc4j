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
 * This message is used to store historic (transmitted afterwards) CTD data .
 */
public class HistoricCTD extends Message {
	public static final int ID_STATIC = 107;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "S/m"
	)
	public float conductivity = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "°C"
	)
	public float temperature = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float depth = 0f;

	public String abbrev() {
		return "HistoricCTD";
	}

	public int mgid() {
		return 107;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(conductivity);
			_out.writeFloat(temperature);
			_out.writeFloat(depth);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			conductivity = buf.getFloat();
			temperature = buf.getFloat();
			depth = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
