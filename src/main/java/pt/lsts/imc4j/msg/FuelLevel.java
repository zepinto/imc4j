package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.TupleList;

/**
 * Report of fuel level.
 */
public class FuelLevel extends Message {
	public static final int ID_STATIC = 279;

	/**
	 * Fuel level percentage of the system.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 100,
			min = 0,
			units = "%"
	)
	public float value = 0f;

	/**
	 * Percentage level of confidence in the estimation of the amount
	 * of energy in the batteries.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 100,
			min = 0,
			units = "%"
	)
	public float confidence = 0f;

	/**
	 * Operation mode name and the estimated time available in that
	 * mode in hours. Example: "Motion=1.5"
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList opmodes = new TupleList("");

	public String abbrev() {
		return "FuelLevel";
	}

	public int mgid() {
		return 279;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(value);
			_out.writeFloat(confidence);
			SerializationUtils.serializePlaintext(_out, opmodes == null? null : opmodes.toString());
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
			confidence = buf.getFloat();
			opmodes = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
