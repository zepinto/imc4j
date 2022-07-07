package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * One Current measurement at a specific CellPosition.
 */
public class CurrentProfileCell extends Message {
	public static final int ID_STATIC = 1015;

	/**
	 * Distance of each measurment cell along the Z-axis in the coordintate frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float cell_position = 0f;

	/**
	 * List of beams measurements at the current cell level.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<ADCPBeam> beams = new ArrayList<>();

	public String abbrev() {
		return "CurrentProfileCell";
	}

	public int mgid() {
		return 1015;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(cell_position);
			SerializationUtils.serializeMsgList(_out, beams);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			cell_position = buf.getFloat();
			beams = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
