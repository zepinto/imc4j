package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.ZUnits;
import pt.lsts.imc.util.SerializationUtils;

/**
 * This message contains the WGS-84 position of a target computed using
 * USBL.
 */
public class UsblFixExtended extends Message {
	public static final int ID_STATIC = 900;

	/**
	 * Target's system name.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String target = "";

	/**
	 * WGS-84 Latitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Units of the z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits z_units = ZUnits.values()[0];

	/**
	 * Target reference in the z axis. Use z_units to specify
	 * whether z represents depth, altitude or other.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * Accuracy of the position fix.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float accuracy = 0f;

	public int mgid() {
		return 900;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, target);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeByte((int)(z_units != null? z_units.value() : 0));
			_out.writeFloat(z);
			_out.writeFloat(accuracy);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			target = SerializationUtils.deserializePlaintext(buf);
			lat = buf.getDouble();
			lon = buf.getDouble();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
			z = buf.getFloat();
			accuracy = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
