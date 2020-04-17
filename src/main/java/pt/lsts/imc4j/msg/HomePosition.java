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

/**
 * Vehicle Home Position.
 */
public class HomePosition extends Message {
	public static final int ID_STATIC = 909;

	/**
	 * Action on the vehicle home
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
		return "HomePosition";
	}

	public int mgid() {
		return 909;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(height);
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
			op = OP.valueOf(buf.get() & 0xFF);
			lat = buf.getDouble();
			lon = buf.getDouble();
			height = buf.getFloat();
			depth = buf.getFloat();
			alt = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
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
