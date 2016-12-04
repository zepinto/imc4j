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
 * Component of incremetal orientation vector over a period of time.
 */
public class EulerAnglesDelta extends Message {
	public static final int ID_STATIC = 255;

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
			units = "rad"
	)
	public double x = 0;

	/**
	 * Y component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double y = 0;

	/**
	 * Z component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double z = 0;

	/**
	 * Period of time of the orientation vector increments.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float timestep = 0f;

	public String abbrev() {
		return "EulerAnglesDelta";
	}

	public int mgid() {
		return 255;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(time);
			_out.writeDouble(x);
			_out.writeDouble(y);
			_out.writeDouble(z);
			_out.writeFloat(timestep);
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
			timestep = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
