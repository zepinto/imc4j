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
import pt.lsts.imc.util.SerializationUtils;

public class Reference extends Message {
	public static final int ID_STATIC = 479;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public DesiredSpeed speed = null;

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public DesiredZ z = null;

	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double lat = 0;

	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double lon = 0;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float radius = 0f;

	public int mgid() {
		return 479;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
			SerializationUtils.serializeInlineMsg(_out, speed);
			SerializationUtils.serializeInlineMsg(_out, z);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(radius);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
			speed = SerializationUtils.deserializeInlineMsg(buf);
			z = SerializationUtils.deserializeInlineMsg(buf);
			lat = buf.getDouble();
			lon = buf.getDouble();
			radius = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FLAGS {
		FLAG_LOCATION(0x01l),

		FLAG_SPEED(0x02l),

		FLAG_Z(0x04l),

		FLAG_RADIUS(0x08l),

		FLAG_START_POINT(0x10l),

		FLAG_DIRECT(0x20l),

		FLAG_MANDONE(0x80l);

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
