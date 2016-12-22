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
import pt.lsts.imc4j.def.CLoopsMask;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * This message summarizes the overall state of the vehicle. It can
 * contains information regarding:
 * - The overall operation mode.
 * - Any error conditions.
 * - Current maneuver execution.
 * - Active control loops.
 */
public class VehicleState extends Message {
	public static final int ID_STATIC = 500;

	/**
	 * The overall operation mode.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP_MODE op_mode = OP_MODE.values()[0];

	/**
	 * Error count for monitored entitites.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int error_count = 0;

	/**
	 * The monitored entities with error conditions. This is a comma
	 * separated list of entity names.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String error_ents = "";

	/**
	 * Type of maneuver being executed, when in MANEUVER mode. The
	 * value is the IMC serialization ID of the corresponding
	 * maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int maneuver_type = 0;

	/**
	 * Start time of maneuver being executed (Epoch time), when in
	 * MANEUVER mode.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double maneuver_stime = 0;

	/**
	 * Estimated time for maneuver completion. The value will be
	 * 65535 if the time is unknown or undefined.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int maneuver_eta = 0;

	/**
	 * Enabled control loops.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "Bitfield"
	)
	public EnumSet<CLoopsMask> control_loops = EnumSet.noneOf(CLoopsMask.class);

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	/**
	 * Description of last error.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String last_error = "";

	/**
	 * Time of last error (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double last_error_time = 0;

	public String abbrev() {
		return "VehicleState";
	}

	public int mgid() {
		return 500;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op_mode != null? op_mode.value() : 0));
			_out.writeByte(error_count);
			SerializationUtils.serializePlaintext(_out, error_ents);
			_out.writeShort(maneuver_type);
			_out.writeDouble(maneuver_stime);
			_out.writeShort(maneuver_eta);
			long _control_loops = 0;
			if (control_loops != null) {
				for (CLoopsMask __control_loops : control_loops.toArray(new CLoopsMask[0])) {
					_control_loops += __control_loops.value();
				}
			}
			_out.writeInt((int)(int)_control_loops);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
			SerializationUtils.serializePlaintext(_out, last_error);
			_out.writeDouble(last_error_time);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			op_mode = OP_MODE.valueOf(buf.get() & 0xFF);
			error_count = buf.get() & 0xFF;
			error_ents = SerializationUtils.deserializePlaintext(buf);
			maneuver_type = buf.getShort() & 0xFFFF;
			maneuver_stime = buf.getDouble();
			maneuver_eta = buf.getShort() & 0xFFFF;
			long control_loops_val = buf.getInt() & 0xFFFFFFFF;
			control_loops.clear();
			for (CLoopsMask CLoopsMask_op : CLoopsMask.values()) {
				if ((control_loops_val & CLoopsMask_op.value()) == CLoopsMask_op.value()) {
					control_loops.add(CLoopsMask_op);
				}
			}
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
			last_error = SerializationUtils.deserializePlaintext(buf);
			last_error_time = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP_MODE {
		VS_SERVICE(0l),

		VS_CALIBRATION(1l),

		VS_ERROR(2l),

		VS_MANEUVER(3l),

		VS_EXTERNAL(4l),

		VS_BOOT(5l);

		protected long value;

		OP_MODE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static OP_MODE valueOf(long value) throws IllegalArgumentException {
			for (OP_MODE v : OP_MODE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for OP_MODE: "+value);
		}
	}

	public enum FLAGS {
		VFLG_MANEUVER_DONE(0x01l);

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
