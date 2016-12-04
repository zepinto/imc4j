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
 * This messages is used to record system activity parameters. These
 * parameters are mainly used for used for maintenance purposes.
 */
public class Tachograph extends Message {
	public static final int ID_STATIC = 905;

	/**
	 * The time when the last service was performed. The number of
	 * seconds is represented in Universal Coordinated Time (UCT) in
	 * seconds since Jan 1, 1970.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double timestamp_last_service = 0;

	/**
	 * Amount of time until the next recommended service.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_next_service = 0f;

	/**
	 * Amount of time the motor can run until the next recommended service.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_motor_next_service = 0f;

	/**
	 * Amount of time the system spent idle on the ground.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_idle_ground = 0f;

	/**
	 * Amount of time the system spent idle in the air.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_idle_air = 0f;

	/**
	 * Amount of time the system spent idle on the water (not submerged).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_idle_water = 0f;

	/**
	 * Amount of time the system spent idle underwater.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_idle_underwater = 0f;

	/**
	 * Amount of time the system spent idle in an unknown medium.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_idle_unknown = 0f;

	/**
	 * Amount of time the system spent on the ground with the motor running.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_motor_ground = 0f;

	/**
	 * Amount of time the system spent in the air with the motor running.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_motor_air = 0f;

	/**
	 * Amount of time the system spent on the water (not submerged) with the motor running.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_motor_water = 0f;

	/**
	 * Amount of time the system spent underwater with the motor running.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_motor_underwater = 0f;

	/**
	 * Amount of time the system spent in an unknown medium with the motor running.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_motor_unknown = 0f;

	/**
	 * The minimum recorded RPM value.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "rpm"
	)
	public int rpm_min = 0;

	/**
	 * The maximum recorded RPM value.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "rpm"
	)
	public int rpm_max = 0;

	/**
	 * The maximum recorded depth value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float depth_max = 0f;

	public String abbrev() {
		return "Tachograph";
	}

	public int mgid() {
		return 905;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(timestamp_last_service);
			_out.writeFloat(time_next_service);
			_out.writeFloat(time_motor_next_service);
			_out.writeFloat(time_idle_ground);
			_out.writeFloat(time_idle_air);
			_out.writeFloat(time_idle_water);
			_out.writeFloat(time_idle_underwater);
			_out.writeFloat(time_idle_unknown);
			_out.writeFloat(time_motor_ground);
			_out.writeFloat(time_motor_air);
			_out.writeFloat(time_motor_water);
			_out.writeFloat(time_motor_underwater);
			_out.writeFloat(time_motor_unknown);
			_out.writeShort(rpm_min);
			_out.writeShort(rpm_max);
			_out.writeFloat(depth_max);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			timestamp_last_service = buf.getDouble();
			time_next_service = buf.getFloat();
			time_motor_next_service = buf.getFloat();
			time_idle_ground = buf.getFloat();
			time_idle_air = buf.getFloat();
			time_idle_water = buf.getFloat();
			time_idle_underwater = buf.getFloat();
			time_idle_unknown = buf.getFloat();
			time_motor_ground = buf.getFloat();
			time_motor_air = buf.getFloat();
			time_motor_water = buf.getFloat();
			time_motor_underwater = buf.getFloat();
			time_motor_unknown = buf.getFloat();
			rpm_min = buf.getShort();
			rpm_max = buf.getShort();
			depth_max = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
