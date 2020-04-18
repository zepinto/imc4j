package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * State of the calibration procedure.
 */
public class DevCalibrationState extends Message {
	public static final int ID_STATIC = 13;

	/**
	 * Total number of steps of the calibration procedure.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int total_steps = 0;

	/**
	 * Number of the current step being performed.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int step_number = 0;

	/**
	 * Human-readable description of the current step.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String step = "";

	/**
	 * Additional flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	public String abbrev() {
		return "DevCalibrationState";
	}

	public int mgid() {
		return 13;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(total_steps);
			_out.writeByte(step_number);
			SerializationUtils.serializePlaintext(_out, step);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			total_steps = buf.get() & 0xFF;
			step_number = buf.get() & 0xFF;
			step = SerializationUtils.deserializePlaintext(buf);
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FLAGS {
		DCS_PREVIOUS_NOT_SUPPORTED(0x01l),

		DCS_NEXT_NOT_SUPPORTED(0x02l),

		DCS_WAITING_CONTROL(0x04l),

		DCS_ERROR(0x08l),

		DCS_COMPLETED(0x10l);

		protected long value;

		FLAGS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static FLAGS valueOf(long value) throws IllegalArgumentException {
			for (FLAGS v : FLAGS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for FLAGS: "+value);
		}
	}
}
