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
 * Report of PID control parcels.
 */
public class ControlParcel extends Message {
	public static final int ID_STATIC = 412;

	/**
	 * Proportional parcel value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float p = 0f;

	/**
	 * Integral parcel value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float i = 0f;

	/**
	 * Derivative parcel value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float d = 0f;

	/**
	 * Anti-windup parcel value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float a = 0f;

	public String abbrev() {
		return "ControlParcel";
	}

	public int mgid() {
		return 412;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(p);
			_out.writeFloat(i);
			_out.writeFloat(d);
			_out.writeFloat(a);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			p = buf.getFloat();
			i = buf.getFloat();
			d = buf.getFloat();
			a = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
