package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * This message presents the simulated state of the vehicle. The simulated
 * state attempts to provide a realistic state interpretation of operating
 * various kinds of vehicles.
 */
public class SimulatedState extends Message {
	public static final int ID_STATIC = 50;

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
	 * The North offset of the North/East/Down field.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * The East offset of the North/East/Down field.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * The Down offset of the North/East/Down field.
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
	 * Body-fixed frame xx axis linear velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float u = 0f;

	/**
	 * Body-fixed frame yy axis linear velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float v = 0f;

	/**
	 * Body-fixed frame zz axis linear velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float w = 0f;

	/**
	 * The angular velocity over body-fixed xx axis (roll rate).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad/s"
	)
	public float p = 0f;

	/**
	 * The angular velocity over body-fixed yy axis (pitch rate).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad/s"
	)
	public float q = 0f;

	/**
	 * The angular velocity over body-fixed zz axis (yaw rate).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad/s"
	)
	public float r = 0f;

	/**
	 * Stream Velocity xx axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float svx = 0f;

	/**
	 * Stream Velocity yy axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float svy = 0f;

	/**
	 * Stream Velocity zz axis velocity component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float svz = 0f;

	public int mgid() {
		return 50;
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
			_out.writeFloat(p);
			_out.writeFloat(q);
			_out.writeFloat(r);
			_out.writeFloat(svx);
			_out.writeFloat(svy);
			_out.writeFloat(svz);
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
			p = buf.getFloat();
			q = buf.getFloat();
			r = buf.getFloat();
			svx = buf.getFloat();
			svy = buf.getFloat();
			svz = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
