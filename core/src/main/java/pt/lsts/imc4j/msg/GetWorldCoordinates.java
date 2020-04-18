package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.Boolean;

/**
 * Message containing the x, y and z coordinates of object in the real world.
 */
public class GetWorldCoordinates extends Message {
	public static final int ID_STATIC = 897;

	/**
	 * True when system is tracking.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public Boolean tracking = Boolean.values()[0];

	/**
	 * Latitude of the real world frame origin.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * Longitude of the real world frame origin.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * X offsets of the target in the real world frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * Y offsets of the target in the real world frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * Z offsets of the target in the real world frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	public String abbrev() {
		return "GetWorldCoordinates";
	}

	public int mgid() {
		return 897;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(tracking != null? tracking.value() : 0));
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			tracking = Boolean.valueOf(buf.get() & 0xFF);
			lat = buf.getDouble();
			lon = buf.getDouble();
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
