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
 * StatusText message from ardupilot.
 */
public class ApmStatus extends Message {
	public static final int ID_STATIC = 906;

	/**
	 * Severity of status.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SEVERITY severity = SEVERITY.values()[0];

	/**
	 * Status text message.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String text = "";

	public String abbrev() {
		return "ApmStatus";
	}

	public int mgid() {
		return 906;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(severity != null? severity.value() : 0));
			SerializationUtils.serializePlaintext(_out, text);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			severity = SEVERITY.valueOf(buf.get() & 0xFF);
			text = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum SEVERITY {
		APM_EMERGENCY(0l),

		APM_ALERT(1l),

		APM_CRITICAL(2l),

		APM_ERROR(3l),

		APM_WARNING(4l),

		APM_NOTICE(5l),

		APM_INFO(6l),

		APM_DEBUG(7l);

		protected long value;

		SEVERITY(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static SEVERITY valueOf(long value) throws IllegalArgumentException {
			for (SEVERITY v : SEVERITY.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for SEVERITY: "+value);
		}
	}
}
