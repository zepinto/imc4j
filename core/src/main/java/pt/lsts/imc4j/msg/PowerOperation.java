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
 * This message allows controlling the system's power lines.
 */
public class PowerOperation extends Message {
	public static final int ID_STATIC = 308;

	/**
	 * Operation type.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Time remaining to complete operation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float time_remain = 0f;

	/**
	 * Scheduled time of operation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double sched_time = 0;

	public String abbrev() {
		return "PowerOperation";
	}

	public int mgid() {
		return 308;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			_out.writeFloat(time_remain);
			_out.writeDouble(sched_time);
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
			time_remain = buf.getFloat();
			sched_time = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		POP_PWR_DOWN(0l),

		POP_PWR_DOWN_IP(1l),

		POP_PWR_DOWN_ABORTED(2l),

		POP_SCHED_PWR_DOWN(3l),

		POP_PWR_UP(4l),

		POP_PWR_UP_IP(5l),

		POP_SCHED_PWR_UP(6l);

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
