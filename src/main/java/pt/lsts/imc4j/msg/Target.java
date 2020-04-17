package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Target.
 */
public class Target extends Message {
	public static final int ID_STATIC = 800;

	/**
	 * Target identifier.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String label = "";

	/**
	 * WGS-84 Latitude coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Z axis reference. Use z_units to specify whether z represents
	 * depth, altitude or other.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * Units of the z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits z_units = ZUnits.values()[0];

	/**
	 * Course Over Ground (true).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float cog = 0f;

	/**
	 * Speed Over Ground.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float sog = 0f;

	public String abbrev() {
		return "Target";
	}

	public int mgid() {
		return 800;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, label);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(z);
			_out.writeByte((int)(z_units != null? z_units.value() : 0));
			_out.writeFloat(cog);
			_out.writeFloat(sog);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			label = SerializationUtils.deserializePlaintext(buf);
			lat = buf.getDouble();
			lon = buf.getDouble();
			z = buf.getFloat();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
			cog = buf.getFloat();
			sog = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
