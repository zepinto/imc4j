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
 * This message will hold an action that needs to be executed at specified time interval.
 */
public class TemporalAction extends Message {
	public static final int ID_STATIC = 911;

	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double start_time = 0;

	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double duration = 0;

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public PlanSpecification action = null;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int system_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATUS status = STATUS.values()[0];

	public String abbrev() {
		return "TemporalAction";
	}

	public int mgid() {
		return 911;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(start_time);
			_out.writeDouble(duration);
			SerializationUtils.serializeInlineMsg(_out, action);
			_out.writeShort(system_id);
			_out.writeByte((int)(status != null? status.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			start_time = buf.getDouble();
			duration = buf.getDouble();
			action = SerializationUtils.deserializeInlineMsg(buf);
			system_id = buf.getShort() & 0xFFFF;
			status = STATUS.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATUS {
		ASTAT_IGNORED(1l),

		ASTAT_SCHEDULED(2l),

		ASTAT_FAILED(3l),

		ASTAT_CANCELLED(4l),

		ASTAT_FINISHED(5l);

		protected long value;

		STATUS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATUS valueOf(long value) throws IllegalArgumentException {
			for (STATUS v : STATUS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATUS: "+value);
		}
	}
}
