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

/**
 * Desired value for each linear and angular speeds.
 */
public class DesiredVelocity extends Message {
	public static final int ID_STATIC = 409;

	/**
	 * Desired speed along the vehicle's x axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double u = 0;

	/**
	 * Desired speed along the vehicle's y axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double v = 0;

	/**
	 * Desired speed along the vehicle's z axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double w = 0;

	/**
	 * Desired speed about the vehicle's x axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double p = 0;

	/**
	 * Desired speed about the vehicle's y axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double q = 0;

	/**
	 * Desired speed about the vehicle's z axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double r = 0;

	/**
	 * Desired Velocity flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	public String abbrev() {
		return "DesiredVelocity";
	}

	public int mgid() {
		return 409;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(u);
			_out.writeDouble(v);
			_out.writeDouble(w);
			_out.writeDouble(p);
			_out.writeDouble(q);
			_out.writeDouble(r);
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
			u = buf.getDouble();
			v = buf.getDouble();
			w = buf.getDouble();
			p = buf.getDouble();
			q = buf.getDouble();
			r = buf.getDouble();
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
		FL_SURGE(0x01l),

		FL_SWAY(0x02l),

		FL_HEAVE(0x04l),

		FL_ROLL(0x08l),

		FL_PITCH(0x10l),

		FL_YAW(0x20l);

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
