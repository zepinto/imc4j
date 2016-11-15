package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * This message is used to store historic (transmitted afterwards) telemetry information.
 */
public class HistoricTelemetry extends Message {
	public static final int ID_STATIC = 108;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float altitude = 0f;

	/**
	 * Roll encoded as α.(65535/(2.π))
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int roll = 0;

	/**
	 * Pitch encoded as α.(65535/(2.π))
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int pitch = 0;

	/**
	 * Yaw encoded as α.(65535/(2.π))
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int yaw = 0;

	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "dm"
	)
	public int speed = 0;

	public int mgid() {
		return 108;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(altitude);
			_out.writeShort(roll);
			_out.writeShort(pitch);
			_out.writeShort(yaw);
			_out.writeShort(speed);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			altitude = buf.getFloat();
			roll = buf.getShort() & 0xFFFF;
			pitch = buf.getShort() & 0xFFFF;
			yaw = buf.getShort() & 0xFFFF;
			speed = buf.getShort();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
