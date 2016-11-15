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
 * Clock control.
 */
public class ClockControl extends Message {
	public static final int ID_STATIC = 106;

	/**
	 * Operation to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Clock value (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double clock = 0;

	/**
	 * Timezone.
	 */
	@FieldType(
			type = IMCField.TYPE_INT8,
			max = 23,
			min = -23
	)
	public int tz = 0;

	public int mgid() {
		return 106;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			_out.writeDouble(clock);
			_out.writeByte(tz);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			op = OP.valueOf(buf.get() & 0xFF);
			clock = buf.getDouble();
			tz = buf.get();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		COP_SYNC_EXEC(0l),

		COP_SYNC_REQUEST(1l),

		COP_SYNC_STARTED(2l),

		COP_SYNC_DONE(3l),

		COP_SET_TZ(4l),

		COP_SET_TZ_DONE(5l);

		protected long value;

		OP(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static OP valueOf(long value) throws IllegalArgumentException {
			for (OP v : OP.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for OP: "+value);
		}
	}
}
