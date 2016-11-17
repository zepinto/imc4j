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
import pt.lsts.imc.util.SerializationUtils;
import pt.lsts.imc.util.TupleList;

/**
 * A "Goto" is a maneuver specifying a movement of the vehicle to a
 * target waypoint. The waypoint is described by the WGS-84
 * waypoint coordinate and target Z reference.
 * Mandatory parameters defined for a "Goto" are
 * timeout, speed and speed units.
 * Optional parameters may be defined for the target Euler
 * Angles (roll, pitch and yaw) though these parameters may
 * not be considered by all maneuver controllers in charge
 * of the execution of this type of maneuver.
 */
public class Goto extends Maneuver {
	public static final int ID_STATIC = 450;

	/**
	 * The amount of time the maneuver is allowed to run.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int timeout = 0;

	/**
	 * WGS-84 Latitude of target waypoint.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude of target waypoint.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

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
	 * Units of the z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ZUnits z_units = ZUnits.values()[0];

	/**
	 * Maneuver speed reference.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float speed = 0f;

	/**
	 * Speed units.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SpeedUnits speed_units = SpeedUnits.values()[0];

	/**
	 * The phi Euler angle in which the vehicle should set its
	 * attitude. Use '-1' for this field to be
	 * unconsidered. Otherwise the value spans from 0 to 2 Pi.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 6.283185307179586,
			min = -1,
			units = "rad"
	)
	public double roll = 0;

	/**
	 * The theta Euler angle in which the vehicle should set its
	 * attitude. Use '-1' for this field to be
	 * disconcerted. Otherwise the value spans from 0 to 2 Pi.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 6.283185307179586,
			min = -1,
			units = "rad"
	)
	public double pitch = 0;

	/**
	 * The psi Euler angle in which the vehicle should set its
	 * attitude. Use '-1' for this field to be considered. Otherwise
	 * the value spans from 0 to 2 Pi.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 6.283185307179586,
			min = -1,
			units = "rad"
	)
	public double yaw = 0;

	/**
	 * Custom settings for maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList custom = new TupleList("");

	public int mgid() {
		return 450;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(timeout);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(z);
			_out.writeByte((int)(z_units != null? z_units.value() : 0));
			_out.writeFloat(speed);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			_out.writeDouble(roll);
			_out.writeDouble(pitch);
			_out.writeDouble(yaw);
			SerializationUtils.serializePlaintext(_out, custom == null? null : custom.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			timeout = buf.getShort() & 0xFFFF;
			lat = buf.getDouble();
			lon = buf.getDouble();
			z = buf.getFloat();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			roll = buf.getDouble();
			pitch = buf.getDouble();
			yaw = buf.getDouble();
			custom = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
