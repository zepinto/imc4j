package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Set the desired virtual forces and torques to be applied to the
 * vehicle.
 */
public class DesiredControl extends Message {
	public static final int ID_STATIC = 407;

	/**
	 * Force X along the vehicle's x axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "N"
	)
	public double x = 0;

	/**
	 * Force Y along the vehicle's y axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "N"
	)
	public double y = 0;

	/**
	 * Force Z along the vehicle's z axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "N"
	)
	public double z = 0;

	/**
	 * Torque K about the vehicle's x axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "Nm"
	)
	public double k = 0;

	/**
	 * Torque M about the vehicle's y axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "Nm"
	)
	public double m = 0;

	/**
	 * Torque N about the vehicle's z axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "Nm"
	)
	public double n = 0;

	/**
	 * Desired Control flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	public String abbrev() {
		return "DesiredControl";
	}

	public int mgid() {
		return 407;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(x);
			_out.writeDouble(y);
			_out.writeDouble(z);
			_out.writeDouble(k);
			_out.writeDouble(m);
			_out.writeDouble(n);
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
			x = buf.getDouble();
			y = buf.getDouble();
			z = buf.getDouble();
			k = buf.getDouble();
			m = buf.getDouble();
			n = buf.getDouble();
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
		FL_X(0x01l),

		FL_Y(0x02l),

		FL_Z(0x04l),

		FL_K(0x08l),

		FL_M(0x10l),

		FL_N(0x20l);

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
