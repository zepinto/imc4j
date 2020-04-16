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
 * Reports VTOL current state.
 */
public class VtolState extends Message {
	public static final int ID_STATIC = 519;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	public String abbrev() {
		return "VtolState";
	}

	public int mgid() {
		return 519;
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
		VTOL_STATE_UNDEFINED(0l),

		VTOL_STATE_TRANSITION_TO_FW(1l),

		VTOL_STATE_TRANSITION_TO_MC(2l),

		VTOL_STATE_MC(3l),

		VTOL_STATE_FW(4l);

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
