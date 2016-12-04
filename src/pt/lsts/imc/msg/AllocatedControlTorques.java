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
 * Control torques allocated to the actuators.
 */
public class AllocatedControlTorques extends Message {
	public static final int ID_STATIC = 411;

	/**
	 * Torque K about the vehicle's x axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "Nm"
	)
	public double k = 0;

	/**
	 * Torque M about the vehicle's y axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "Nm"
	)
	public double m = 0;

	/**
	 * Torque N about the vehicle's z axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "Nm"
	)
	public double n = 0;

	public String abbrev() {
		return "AllocatedControlTorques";
	}

	public int mgid() {
		return 411;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(k);
			_out.writeDouble(m);
			_out.writeDouble(n);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			k = buf.getDouble();
			m = buf.getDouble();
			n = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
