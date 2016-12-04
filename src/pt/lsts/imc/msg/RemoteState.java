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
 * State summary for a remote vehicle.
 */
public class RemoteState extends Message {
	public static final int ID_STATIC = 750;

	/**
	 * WGS-84 Latitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public float lat = 0f;

	/**
	 * WGS-84 Longitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public float lon = 0f;

	/**
	 * Depth.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "m"
	)
	public int depth = 0;

	/**
	 * Speed.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float speed = 0f;

	/**
	 * Heading.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float psi = 0f;

	public String abbrev() {
		return "RemoteState";
	}

	public int mgid() {
		return 750;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(lat);
			_out.writeFloat(lon);
			_out.writeByte(depth);
			_out.writeFloat(speed);
			_out.writeFloat(psi);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			lat = buf.getFloat();
			lon = buf.getFloat();
			depth = buf.get() & 0xFF;
			speed = buf.getFloat();
			psi = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
