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
 * Set properties of a PWM signal channel.
 */
public class SetPWM extends Message {
	public static final int ID_STATIC = 315;

	/**
	 * PWM channel identifier.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int id = 0;

	/**
	 * The total period of the PWM signal (sum of active and inactive
	 * time of the PWM).
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "µs"
	)
	public long period = 0;

	/**
	 * The active time of the PWM signal. The duty cycle value must
	 * be less or equal to the period.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "µs"
	)
	public long duty_cycle = 0;

	public String abbrev() {
		return "SetPWM";
	}

	public int mgid() {
		return 315;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(id);
			_out.writeInt((int)period);
			_out.writeInt((int)duty_cycle);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			id = buf.get() & 0xFF;
			period = buf.getInt() & 0xFFFFFFFF;
			duty_cycle = buf.getInt() & 0xFFFFFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
