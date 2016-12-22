package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.SpeedUnits;

/**
 * Desired Speed reference value for the control layer.
 */
public class DesiredSpeed extends ControlCommand {
	public static final int ID_STATIC = 402;

	/**
	 * The value of the desired speed, in the scale specified by the
	 * "Speed Units" field.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double value = 0;

	/**
	 * Indicates the units used for the speed value.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SpeedUnits speed_units = SpeedUnits.values()[0];

	public String abbrev() {
		return "DesiredSpeed";
	}

	public int mgid() {
		return 402;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(value);
			_out.writeByte((int)(speed_units != null? speed_units.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getDouble();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
