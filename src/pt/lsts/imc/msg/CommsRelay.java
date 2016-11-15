package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.SpeedUnits;

/**
 * In this maneuver, a vehicle drives to the center of two other
 * systems (a, b) in order to be used as a communications relay.
 */
public class CommsRelay extends Maneuver {
	public static final int ID_STATIC = 472;

	/**
	 * WGS-84 Latitude for start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude for start point.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

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
	 * Duration of maneuver, 0 for unlimited duration.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int duration = 0;

	/**
	 * The IMC id of the system A.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int sys_a = 0;

	/**
	 * The IMC id of the system B.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int sys_b = 0;

	/**
	 * Move only if the distance to the target is bigger than this
	 * threshold.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float move_threshold = 0f;

	public int mgid() {
		return 472;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(speed);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			_out.writeShort(duration);
			_out.writeShort(sys_a);
			_out.writeShort(sys_b);
			_out.writeFloat(move_threshold);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			lat = buf.getDouble();
			lon = buf.getDouble();
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			duration = buf.getShort() & 0xFFFF;
			sys_a = buf.getShort() & 0xFFFF;
			sys_b = buf.getShort() & 0xFFFF;
			move_threshold = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
