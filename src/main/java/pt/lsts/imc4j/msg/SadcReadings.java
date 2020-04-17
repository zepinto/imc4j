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
 * Readings from SADC board.
 */
public class SadcReadings extends Message {
	public static final int ID_STATIC = 907;

	/**
	 * Channel of SADC to read.
	 */
	@FieldType(
			type = IMCField.TYPE_INT8,
			max = 4,
			min = 1
	)
	public int channel = 0;

	/**
	 * Value raw of sadc channel.
	 */
	@FieldType(
			type = IMCField.TYPE_INT32
	)
	public int value = 0;

	/**
	 * Gain value of readings.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public GAIN gain = GAIN.values()[0];

	public String abbrev() {
		return "SadcReadings";
	}

	public int mgid() {
		return 907;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(channel);
			_out.writeInt((int)value);
			_out.writeByte((int)(gain != null? gain.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			channel = buf.get();
			value = buf.getInt();
			gain = GAIN.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum GAIN {
		GAIN_X1(0l),

		GAIN_X10(1l),

		GAIN_X100(2l);

		protected long value;

		GAIN(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static GAIN valueOf(long value) throws IllegalArgumentException {
			for (GAIN v : GAIN.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for GAIN: "+value);
		}
	}
}
