package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

/**
 * Report of spatial orientation according to SNAME's notation
 * (1950).
 */
public class EulerAngles extends Message {
	public static final int ID_STATIC = 254;

	/**
	 * The device time.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double time = 0;

	/**
	 * Rotation around the vehicle longitudinal axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double phi = 0;

	/**
	 * Rotation around the vehicle lateral or transverse axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.57079632679490,
			min = -1.57079632679490,
			units = "rad"
	)
	public double theta = 0;

	/**
	 * Rotation around the vehicle vertical axis. A value of 0 means
	 * the vehicle is oriented towards true north. In cases where the
	 * sensor cannot measure the true heading, this field will have
	 * the same value as Yaw (Magnetic).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double psi = 0;

	/**
	 * Rotation around the vehicle vertical axis. A value of 0 means
	 * the vehicle is oriented towards magnetic north. In cases where
	 * the sensor cannot measure the magnetic heading, this field
	 * will have the same value as Yaw (True).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double psi_magnetic = 0;

	public String abbrev() {
		return "EulerAngles";
	}

	public int mgid() {
		return 254;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(time);
			_out.writeDouble(phi);
			_out.writeDouble(theta);
			_out.writeDouble(psi);
			_out.writeDouble(psi_magnetic);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			time = buf.getDouble();
			phi = buf.getDouble();
			theta = buf.getDouble();
			psi = buf.getDouble();
			psi_magnetic = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
