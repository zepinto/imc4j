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
 * This message presents the estimated state of the vehicle.
 * EstimatedState is a complete description of the system
 * in terms of parameters such as position, orientation and
 * velocities at a particular moment in time.
 * The system position is given by a North-East-Down (NED)
 * local tangent plane displacement (x, y, z) relative to
 * an absolute WGS-84 coordinate (latitude, longitude,
 * height above ellipsoid).
 * The symbols for position and attitude as well as linear and
 * angular velocities were chosen according to SNAME's notation (1950).
 * The body-fixed reference frame and Euler angles are depicted
 * next:
 * .. figure:: ../images/euler-lauv.png
 * :align:  center
 * Euler angles
 */
public class EstimatedState extends Message {
	public static final int ID_STATIC = 350;

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
	 * Height above the WGS-84 ellipsoid.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float height = 0f;

	/**
	 * The North offset of the North/East/Down field with respect to
	 * LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * The East offset of the North/East/Down field with respect to
	 * LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * The Down offset of the North/East/Down field with respect to
	 * LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float z = 0f;

	/**
	 * The phi Euler angle from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public float phi = 0f;

	/**
	 * The theta Euler angle from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 1.57079632679490,
			min = -1.57079632679490,
			units = "rad"
	)
	public float theta = 0f;

	/**
	 * The psi Euler angle from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public float psi = 0f;

	/**
	 * Body-fixed frame xx axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float u = 0f;

	/**
	 * Body-fixed frame yy axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float v = 0f;

	/**
	 * Body-fixed frame zz axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float w = 0f;

	/**
	 * Ground Velocity xx axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vx = 0f;

	/**
	 * Ground Velocity yy axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vy = 0f;

	/**
	 * Ground Velocity zz axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vz = 0f;

	/**
	 * The angular velocity over body-fixed xx axis (roll).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad/s"
	)
	public float p = 0f;

	/**
	 * The angular velocity over body-fixed yy axis (pitch).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad/s"
	)
	public float q = 0f;

	/**
	 * The angular velocity over body-fixed zz axis (yaw).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad/s"
	)
	public float r = 0f;

	/**
	 * Depth, in meters. To be used by underwater vehicles. Negative
	 * values denote invalid estimates.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float depth = 0f;

	/**
	 * Altitude, in meters. Negative values denote invalid estimates.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float alt = 0f;

	public String abbrev() {
		return "EstimatedState";
	}

	public int mgid() {
		return 350;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(height);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			_out.writeFloat(phi);
			_out.writeFloat(theta);
			_out.writeFloat(psi);
			_out.writeFloat(u);
			_out.writeFloat(v);
			_out.writeFloat(w);
			_out.writeFloat(vx);
			_out.writeFloat(vy);
			_out.writeFloat(vz);
			_out.writeFloat(p);
			_out.writeFloat(q);
			_out.writeFloat(r);
			_out.writeFloat(depth);
			_out.writeFloat(alt);
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
			height = buf.getFloat();
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
			phi = buf.getFloat();
			theta = buf.getFloat();
			psi = buf.getFloat();
			u = buf.getFloat();
			v = buf.getFloat();
			w = buf.getFloat();
			vx = buf.getFloat();
			vy = buf.getFloat();
			vz = buf.getFloat();
			p = buf.getFloat();
			q = buf.getFloat();
			r = buf.getFloat();
			depth = buf.getFloat();
			alt = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
