package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.Boolean;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.TupleList;

/**
 * The Handover maneuver allows the vehicle to go to a "remote" location, where the control of the vehicle is handed
 * over to another basestation and/or safety pilot.
 */
public class Handover extends Maneuver {
	public static final int ID_STATIC = 495;

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
	 * Maneuver reference in the z axis. Use z_units to specify
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
	 * Radius of the maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 100000,
			min = 1,
			units = "m"
	)
	public float radius = 0f;

	/**
	 * Desired direction.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 3,
			units = "Enumerated"
	)
	public DIRECTION direction = DIRECTION.values()[0];

	/**
	 * Remote control Rx/Tx handover.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public Boolean rc_handover = Boolean.values()[0];

	/**
	 * Custom settings for maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList custom = new TupleList("");

	public String abbrev() {
		return "Handover";
	}

	public int mgid() {
		return 495;
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
			_out.writeFloat(radius);
			_out.writeByte((int)(direction != null? direction.value() : 0));
			_out.writeByte((int)(rc_handover != null? rc_handover.value() : 0));
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
			radius = buf.getFloat();
			direction = DIRECTION.valueOf(buf.get() & 0xFF);
			rc_handover = Boolean.valueOf(buf.get() & 0xFF);
			custom = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum DIRECTION {
		LD_CLOCKW(0l),

		LD_CCLOCKW(1l);

		protected long value;

		DIRECTION(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static DIRECTION valueOf(long value) throws IllegalArgumentException {
			for (DIRECTION v : DIRECTION.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for DIRECTION: "+value);
		}
	}
}
