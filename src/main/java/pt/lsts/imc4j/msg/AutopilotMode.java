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
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Reports autopilot mode.
 */
public class AutopilotMode extends Message {
	public static final int ID_STATIC = 511;

	/**
	 * Current mode autonomy level.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public AUTONOMY autonomy = AUTONOMY.values()[0];

	/**
	 * Current mode name.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String mode = "";

	public String abbrev() {
		return "AutopilotMode";
	}

	public int mgid() {
		return 511;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(autonomy != null? autonomy.value() : 0));
			SerializationUtils.serializePlaintext(_out, mode);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			autonomy = AUTONOMY.valueOf(buf.get() & 0xFF);
			mode = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum AUTONOMY {
		AL_MANUAL(0l),

		AL_ASSISTED(1l),

		AL_AUTO(2l);

		protected long value;

		AUTONOMY(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static AUTONOMY valueOf(long value) throws IllegalArgumentException {
			for (AUTONOMY v : AUTONOMY.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for AUTONOMY: "+value);
		}
	}
}
