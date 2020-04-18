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
 * This message allows controlling power channels.
 */
public class PowerChannelControl extends Message {
	public static final int ID_STATIC = 309;

	/**
	 * The name of the power channel.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	/**
	 * Operation to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 6,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Scheduled time of operation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double sched_time = 0;

	public String abbrev() {
		return "PowerChannelControl";
	}

	public int mgid() {
		return 309;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			_out.writeByte((int)(op != null? op.value() : 0));
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
			name = SerializationUtils.deserializePlaintext(buf);
			op = OP.valueOf(buf.get() & 0xFF);
			sched_time = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		PCC_OP_TURN_OFF(0l),

		PCC_OP_TURN_ON(1l),

		PCC_OP_TOGGLE(2l),

		PCC_OP_SCHED_ON(3l),

		PCC_OP_SCHED_OFF(4l),

		PCC_OP_SCHED_RESET(5l),

		PCC_OP_SAVE(6l),

		PCC_OP_RESTART(7l);

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
