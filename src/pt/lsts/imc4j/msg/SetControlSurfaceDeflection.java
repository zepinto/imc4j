package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

/**
 * Set the deflection angle of a control surface.
 */
public class SetControlSurfaceDeflection extends Message {
	public static final int ID_STATIC = 303;

	/**
	 * The identification number of the destination control surface.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int id = 0;

	/**
	 * Actuation magnitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float angle = 0f;

	public String abbrev() {
		return "SetControlSurfaceDeflection";
	}

	public int mgid() {
		return 303;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(id);
			_out.writeFloat(angle);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			id = buf.get() & 0xFF;
			angle = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
