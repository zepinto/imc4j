package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Dissolved Organic Matter measurement.
 */
public class DissolvedOrganicMatter extends Message {
	public static final int ID_STATIC = 903;

	/**
	 * Dissolved Organic Matter reading.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "PPB"
	)
	public float value = 0f;

	/**
	 * Type of measurement.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	public int mgid() {
		return 903;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(value);
			_out.writeByte((int)(type != null? type.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getFloat();
			type = TYPE.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		DT_COLORED(0l),

		DT_FLUORESCENT(1l);

		protected long value;

		TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static TYPE valueOf(long value) throws IllegalArgumentException {
			for (TYPE v : TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for TYPE: "+value);
		}
	}
}
