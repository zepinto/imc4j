package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Maneuver control state.
 */
public class ManeuverControlState extends Message {
	public static final int ID_STATIC = 470;

	/**
	 * Code indicating maneuver state.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	/**
	 * Estimated time to completion of the maneuver, when executing.
	 * The value will be 65535 if the time is unknown or undefined.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int eta = 0;

	/**
	 * Complementary information, e.g., regarding errors.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String info = "";

	public String abbrev() {
		return "ManeuverControlState";
	}

	public int mgid() {
		return 470;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(state != null? state.value() : 0));
			_out.writeShort(eta);
			SerializationUtils.serializePlaintext(_out, info);
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
			eta = buf.getShort() & 0xFFFF;
			info = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		MCS_EXECUTING(0l),

		MCS_DONE(1l),

		MCS_ERROR(2l),

		MCS_STOPPED(3l);

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
