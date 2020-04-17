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
 * Report sanity or lack of it in the data output by a sensor.
 */
public class DataSanity extends Message {
	public static final int ID_STATIC = 284;

	/**
	 * Whether the data is sane or not sane.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SANE sane = SANE.values()[0];

	public String abbrev() {
		return "DataSanity";
	}

	public int mgid() {
		return 284;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(sane != null? sane.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			sane = SANE.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum SANE {
		DS_SANE(0l),

		DS_NOT_SANE(1l);

		protected long value;

		SANE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static SANE valueOf(long value) throws IllegalArgumentException {
			for (SANE v : SANE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for SANE: "+value);
		}
	}
}
