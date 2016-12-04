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
 * Report of GPS navigation data.
 */
public class GpsNavData extends Message {
	public static final int ID_STATIC = 280;

	/**
	 * GPS Millisecond Time of Week.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "ms"
	)
	public long itow = 0;

	/**
	 * Latitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * Longitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Height Above Ellipsoid.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float height_ell = 0f;

	/**
	 * Height Above Sea Level.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float height_sea = 0f;

	/**
	 * Horizontal Accuracy Estimate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float hacc = 0f;

	/**
	 * Vertical Accuracy Estimate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float vacc = 0f;

	/**
	 * NED North Velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vel_n = 0f;

	/**
	 * NED East Velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vel_e = 0f;

	/**
	 * NED Down Velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vel_d = 0f;

	/**
	 * NED Down Velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float speed = 0f;

	/**
	 * NED Down Velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float gspeed = 0f;

	/**
	 * NED Down Velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float heading = 0f;

	/**
	 * NED Down Velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float sacc = 0f;

	/**
	 * Course / Heading Accuracy Estimate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float cacc = 0f;

	public String abbrev() {
		return "GpsNavData";
	}

	public int mgid() {
		return 280;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)itow);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(height_ell);
			_out.writeFloat(height_sea);
			_out.writeFloat(hacc);
			_out.writeFloat(vacc);
			_out.writeFloat(vel_n);
			_out.writeFloat(vel_e);
			_out.writeFloat(vel_d);
			_out.writeFloat(speed);
			_out.writeFloat(gspeed);
			_out.writeFloat(heading);
			_out.writeFloat(sacc);
			_out.writeFloat(cacc);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			itow = buf.getInt() & 0xFFFFFFFF;
			lat = buf.getDouble();
			lon = buf.getDouble();
			height_ell = buf.getFloat();
			height_sea = buf.getFloat();
			hacc = buf.getFloat();
			vacc = buf.getFloat();
			vel_n = buf.getFloat();
			vel_e = buf.getFloat();
			vel_d = buf.getFloat();
			speed = buf.getFloat();
			gspeed = buf.getFloat();
			heading = buf.getFloat();
			sacc = buf.getFloat();
			cacc = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
