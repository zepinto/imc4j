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
 * Reports if motors are currently armed or disarmed.
 */
public class ArmingState extends Message {
	public static final int ID_STATIC = 520;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	public String abbrev() {
		return "ArmingState";
	}

	public int mgid() {
		return 520;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(state != null? state.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			state = STATE.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		MOTORS_ARMED(0l),

		MOTORS_DISARMED(1l);

		protected long value;

		STATE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATE valueOf(long value) throws IllegalArgumentException {
			for (STATE v : STATE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATE: "+value);
		}
	}
}
