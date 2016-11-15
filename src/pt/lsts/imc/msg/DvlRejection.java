package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * When the vehicle uses Doppler Velocity Log sensor, this message
 * notifies that a new measurement was locally rejected by the
 * navigation filter.
 */
public class DvlRejection extends Message {
	public static final int ID_STATIC = 358;

	/**
	 * This field represents the type of the rejected velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<TYPE> type = EnumSet.noneOf(TYPE.class);

	/**
	 * Reason for rejection. There are two types of DVL measurement
	 * filters. An Innovation filter checks the innovation between
	 * the current measurement and the previous measurement within a
	 * certain amount of time and an Absolute filter compares the
	 * measurement with an absolute threshold value. Those filters
	 * are tested using horizontal speed measurements, i.e.,
	 * measurements in the x-axis and in the y-axis.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public REASON reason = REASON.values()[0];

	/**
	 * Value of the rejection.
	 * If it is an innovation rejection the value is
	 * the absolute difference between the previous
	 * accepted DVL measurement and the current one.
	 * If it is an absolute rejection the value is
	 * the current DVL measurement.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float value = 0f;

	/**
	 * Timestep of the rejection.
	 * The timestep is 0 for an absolute rejection
	 * since it is an instantaneous reading. For
	 * innovation rejection it is the time difference
	 * between the previous accepted DVL measurement
	 * and the current one.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float timestep = 0f;

	public int mgid() {
		return 358;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			long _type = 0;
			if (type != null) {
				for (TYPE __type : type.toArray(new TYPE[0])) {
					_type += __type.value();
				}
			}
			_out.writeByte((int)_type);
			_out.writeByte((int)(reason != null? reason.value() : 0));
			_out.writeFloat(value);
			_out.writeFloat(timestep);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			long type_val = buf.get() & 0xFF;
			type.clear();
			for (TYPE TYPE_op : TYPE.values()) {
				if ((type_val & TYPE_op.value()) == TYPE_op.value()) {
					type.add(TYPE_op);
				}
			}
			reason = REASON.valueOf(buf.get() & 0xFF);
			value = buf.getFloat();
			timestep = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		TYPE_GV(0x01l),

		TYPE_WV(0x02l);

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

	public enum REASON {
		RR_INNOV_THRESHOLD_X(0l),

		RR_INNOV_THRESHOLD_Y(1l),

		RR_ABS_THRESHOLD_X(2l),

		RR_ABS_THRESHOLD_Y(3l);

		protected long value;

		REASON(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static REASON valueOf(long value) throws IllegalArgumentException {
			for (REASON v : REASON.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for REASON: "+value);
		}
	}
}
