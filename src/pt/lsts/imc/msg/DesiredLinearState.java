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
 * Position, velocity and acceleration setpoints in NED
 */
public class DesiredLinearState extends Message {
	public static final int ID_STATIC = 414;

	/**
	 * Desired pos in x.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m"
	)
	public double x = 0;

	/**
	 * Desired pos in y.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m"
	)
	public double y = 0;

	/**
	 * Desired pos in z.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m"
	)
	public double z = 0;

	/**
	 * Desired speed along NED x axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double vx = 0;

	/**
	 * Desired speed along NED y axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double vy = 0;

	/**
	 * Desired speed along NED z axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double vz = 0;

	/**
	 * Desired acceleration along NED x axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s/s"
	)
	public double ax = 0;

	/**
	 * Desired acceleration along NED y axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s/s"
	)
	public double ay = 0;

	/**
	 * Desired acceleration along NED z axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s/s"
	)
	public double az = 0;

	/**
	 * Setpoint Flags
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	public int mgid() {
		return 414;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(x);
			_out.writeDouble(y);
			_out.writeDouble(z);
			_out.writeDouble(vx);
			_out.writeDouble(vy);
			_out.writeDouble(vz);
			_out.writeDouble(ax);
			_out.writeDouble(ay);
			_out.writeDouble(az);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeShort((int)_flags);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			x = buf.getDouble();
			y = buf.getDouble();
			z = buf.getDouble();
			vx = buf.getDouble();
			vy = buf.getDouble();
			vz = buf.getDouble();
			ax = buf.getDouble();
			ay = buf.getDouble();
			az = buf.getDouble();
			long flags_val = buf.getShort() & 0xFFFF;
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
		FL_X(0x0001l),

		FL_Y(0x0002l),

		FL_Z(0x0004l),

		FL_VX(0x0008l),

		FL_VY(0x0010l),

		FL_VZ(0x0020l),

		FL_AX(0x0040l),

		FL_AY(0x0080l),

		FL_AZ(0x0100l);

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
