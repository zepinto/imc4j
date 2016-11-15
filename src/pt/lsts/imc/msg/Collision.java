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
 * Detected collision.
 */
public class Collision extends Message {
	public static final int ID_STATIC = 509;

	/**
	 * Estimated collision acceleration value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s/s"
	)
	public float value = 0f;

	/**
	 * Collision flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<TYPE> type = EnumSet.noneOf(TYPE.class);

	public int mgid() {
		return 509;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(value);
			long _type = 0;
			for (TYPE __type : type.toArray(new TYPE[0])) {
				_type += __type.value();
			}
			_out.writeByte((int)_type);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getFloat();
			long type_val = buf.get() & 0xFF;
			type.clear();
			for (TYPE TYPE_op : TYPE.values()) {
				if ((type_val & TYPE_op.value()) == TYPE_op.value()) {
					type.add(TYPE_op);
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		CD_X(0x01l),

		CD_Y(0x02l),

		CD_Z(0x04l),

		CD_IMPACT(0x08l);

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
}
