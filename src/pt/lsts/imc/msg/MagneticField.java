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
 * Vector quantifying the direction and magnitude of the measured
 * magnetic field that a device is exposed to.
 */
public class MagneticField extends Message {
	public static final int ID_STATIC = 258;

	/**
	 * The device time.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double time = 0;

	/**
	 * X component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "G"
	)
	public double x = 0;

	/**
	 * Y component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "G"
	)
	public double y = 0;

	/**
	 * Z component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "G"
	)
	public double z = 0;

	public String abbrev() {
		return "MagneticField";
	}

	public int mgid() {
		return 258;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(time);
			_out.writeDouble(x);
			_out.writeDouble(y);
			_out.writeDouble(z);
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
			x = buf.getDouble();
			y = buf.getDouble();
			z = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
