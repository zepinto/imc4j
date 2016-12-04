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

public class GpsFixRejection extends Message {
	public static final int ID_STATIC = 356;

	/**
	 * UTC time of the rejected GPS fix measured in seconds since
	 * 00:00:00 (midnight).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float utc_time = 0f;

	/**
	 * Reason for rejection.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public REASON reason = REASON.values()[0];

	public String abbrev() {
		return "GpsFixRejection";
	}

	public int mgid() {
		return 356;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(utc_time);
			_out.writeByte((int)(reason != null? reason.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			utc_time = buf.getFloat();
			reason = REASON.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum REASON {
		RR_ABOVE_THRESHOLD(0l),

		RR_INVALID(1l),

		RR_ABOVE_MAX_HDOP(2l),

		RR_ABOVE_MAX_HACC(3l),

		RR_LOST_VAL_BIT(4l);

		protected long value;

		REASON(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static REASON valueOf(long value) throws IllegalArgumentException {
			for (REASON v : REASON.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for REASON: "+value);
		}
	}
}
