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
 * Vector quantifying the direction and magnitude of the measured
 * velocity relative to the ground that a device is exposed to.
 */
public class GroundVelocity extends Message {
	public static final int ID_STATIC = 259;

	/**
	 * Each bit of this field represents if a given velocity
	 * component is valid.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<VALIDITY> validity = EnumSet.noneOf(VALIDITY.class);

	/**
	 * X component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double x = 0;

	/**
	 * Y component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double y = 0;

	/**
	 * Z component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m/s"
	)
	public double z = 0;

	public String abbrev() {
		return "GroundVelocity";
	}

	public int mgid() {
		return 259;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			long _validity = 0;
			if (validity != null) {
				for (VALIDITY __validity : validity.toArray(new VALIDITY[0])) {
					_validity += __validity.value();
				}
			}
			_out.writeByte((int)_validity);
			_out.writeDouble(x);
			_out.writeDouble(y);
			_out.writeDouble(z);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			long validity_val = buf.get() & 0xFF;
			validity.clear();
			for (VALIDITY VALIDITY_op : VALIDITY.values()) {
				if ((validity_val & VALIDITY_op.value()) == VALIDITY_op.value()) {
					validity.add(VALIDITY_op);
				}
			}
			x = buf.getDouble();
			y = buf.getDouble();
			z = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum VALIDITY {
		VAL_VEL_X(0x01l),

		VAL_VEL_Y(0x02l),

		VAL_VEL_Z(0x04l);

		protected long value;

		VALIDITY(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static VALIDITY valueOf(long value) throws IllegalArgumentException {
			for (VALIDITY v : VALIDITY.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for VALIDITY: "+value);
		}
	}
}
