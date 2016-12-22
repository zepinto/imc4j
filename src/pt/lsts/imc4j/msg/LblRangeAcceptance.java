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
 * When the vehicle uses Long Base Line navigation, this message
 * notifies that a new range was received from one of the acoustics
 * transponders. The message fields are used to identify the range
 * value and the transponder name. Also, this message has an
 * acceptance field that indicates whether a LBL range was accepted
 * or rejected, and if rejected, the reason why.
 */
public class LblRangeAcceptance extends Message {
	public static final int ID_STATIC = 357;

	/**
	 * Identification number of the acoustic transponder from which
	 * the range information was received.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int id = 0;

	/**
	 * Distance to the acoustic transponder.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float range = 0f;

	/**
	 * Reason for acceptance/rejection.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ACCEPTANCE acceptance = ACCEPTANCE.values()[0];

	public String abbrev() {
		return "LblRangeAcceptance";
	}

	public int mgid() {
		return 357;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(id);
			_out.writeFloat(range);
			_out.writeByte((int)(acceptance != null? acceptance.value() : 0));
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
			range = buf.getFloat();
			acceptance = ACCEPTANCE.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ACCEPTANCE {
		RR_ACCEPTED(0l),

		RR_ABOVE_THRESHOLD(1l),

		RR_SINGULAR(2l),

		RR_NO_INFO(3l),

		RR_AT_SURFACE(4l);

		protected long value;

		ACCEPTANCE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ACCEPTANCE valueOf(long value) throws IllegalArgumentException {
			for (ACCEPTANCE v : ACCEPTANCE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ACCEPTANCE: "+value);
		}
	}
}
