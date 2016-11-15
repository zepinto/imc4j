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
 * Detect current vehicle medium.
 */
public class VehicleMedium extends Message {
	public static final int ID_STATIC = 508;

	/**
	 * Current medium.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public MEDIUM medium = MEDIUM.values()[0];

	public int mgid() {
		return 508;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)medium.value());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			medium = MEDIUM.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum MEDIUM {
		VM_GROUND(0l),

		VM_AIR(1l),

		VM_WATER(2l),

		VM_UNDERWATER(3l),

		VM_UNKNOWN(4l);

		protected long value;

		MEDIUM(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static MEDIUM valueOf(long value) throws IllegalArgumentException {
			for (MEDIUM v : MEDIUM.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for MEDIUM: "+value);
		}
	}
}
