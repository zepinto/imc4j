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
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * This message defines the formation leader state.
 * LeaderState is a complete description of the leader state
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
public class LeaderState extends Message {
	public static final int ID_STATIC = 563;

	/**
	 * Name for the formation group.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String group_name = "";

	/**
	 * Action on the formation leader state variables
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

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

	public String abbrev() {
		return "LeaderState";
	}

	public int mgid() {
		return 563;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, group_name);
			_out.writeByte((int)(op != null? op.value() : 0));
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(height);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(z);
			_out.writeFloat(phi);
			_out.writeFloat(theta);
			_out.writeFloat(psi);
			_out.writeFloat(vx);
			_out.writeFloat(vy);
			_out.writeFloat(vz);
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
			group_name = SerializationUtils.deserializePlaintext(buf);
			op = OP.valueOf(buf.get() & 0xFF);
			lat = buf.getDouble();
			lon = buf.getDouble();
			height = buf.getFloat();
			x = buf.getFloat();
			y = buf.getFloat();
			z = buf.getFloat();
			phi = buf.getFloat();
			theta = buf.getFloat();
			psi = buf.getFloat();
			vx = buf.getFloat();
			vy = buf.getFloat();
			vz = buf.getFloat();
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

	public enum OP {
		OP_REQUEST(0l),

		OP_SET(1l),

		OP_REPORT(2l);

		protected long value;

		OP(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static OP valueOf(long value) throws IllegalArgumentException {
			for (OP v : OP.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for OP: "+value);
		}
	}
}
