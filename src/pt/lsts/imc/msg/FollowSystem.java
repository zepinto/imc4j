package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.def.ZUnits;

/**
 * System-following maneuver.
 */
public class FollowSystem extends Message {
	public static final int ID_STATIC = 471;

	/**
	 * IMC address of system to follow.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int system = 0;

	/**
	 * Duration of maneuver, 0 for unlimited duration.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int duration = 0;

	/**
	 * Reference speed.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float speed = 0f;

	/**
	 * Reference speed units.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SpeedUnits speed_units = SpeedUnits.values()[0];

	/**
	 * Along-track offset.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float x = 0f;

	/**
	 * Cross-track offset.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float y = 0f;

	/**
	 * Coordinate z during follow maneuver. Use z_units to specify
	 * whether z represents depth, altitude or other.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
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

	public int mgid() {
		return 471;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(system);
			_out.writeShort(duration);
			_out.writeFloat(speed);
			_out.writeByte((int)speed_units.value());
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			_out.writeByte((int)z_units.value());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			system = buf.getShort() & 0xFFFF;
			duration = buf.getShort() & 0xFFFF;
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
