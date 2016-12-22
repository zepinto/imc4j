package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.TupleList;

/**
 * Automatic landing on the ground, for UAVs.
 * This maneuver specifies the target touchdown location and sets the final approach based on the maneuver bearing and glide slope parameters.
 */
public class Land extends Maneuver {
	public static final int ID_STATIC = 492;

	/**
	 * WGS-84 Latitude of touchdown waypoint.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude of touchdown waypoint.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Target altitude or height for the automatic landing.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * Units of the z reference and abort z reference.
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
	 * Abort altitude or height. If landing is aborted while executing, the UAV will maintain its course and attempt to climb to the abort z reference.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float abort_z = 0f;

	/**
	 * Land bearing angle.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 6.283185307179586,
			min = 0,
			units = "rad"
	)
	public double bearing = 0;

	/**
	 * Ratio of the distance from the last waypoint to the landing point (touchdown) and the height difference between them.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 10,
			units = "%"
	)
	public int glide_slope = 0;

	/**
	 * Height difference between the last waypoint to the landing point (touchdown).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float glide_slope_alt = 0f;

	/**
	 * Custom settings for maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList custom = new TupleList("");

	public String abbrev() {
		return "Land";
	}

	public int mgid() {
		return 492;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(z);
			_out.writeByte((int)(z_units != null? z_units.value() : 0));
			_out.writeFloat(speed);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			_out.writeFloat(abort_z);
			_out.writeDouble(bearing);
			_out.writeByte(glide_slope);
			_out.writeFloat(glide_slope_alt);
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
			lat = buf.getDouble();
			lon = buf.getDouble();
			z = buf.getFloat();
			z_units = ZUnits.valueOf(buf.get() & 0xFF);
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			abort_z = buf.getFloat();
			bearing = buf.getDouble();
			glide_slope = buf.get() & 0xFF;
			glide_slope_alt = buf.getFloat();
			custom = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
