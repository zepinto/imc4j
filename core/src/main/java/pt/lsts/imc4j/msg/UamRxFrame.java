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
 * This message shall be dispatched by acoustic modem drivers each time
 * a data frame is received over the acoustic channel.
 */
public class UamRxFrame extends Message {
	public static final int ID_STATIC = 815;

	/**
	 * The canonical name of the node that transmitted the data frame. If
	 * this name cannot be resolved the string 'unknown' shall be used.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_src = "";

	/**
	 * The canonical name of the destination node of the data frame. If
	 * this name cannot be resolved the string 'unknown' shall be used.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_dst = "";

	/**
	 * Reception flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	/**
	 * The actual received data frame.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "UamRxFrame";
	}

	public int mgid() {
		return 815;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, sys_src);
			SerializationUtils.serializePlaintext(_out, sys_dst);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeByte((int)_flags);
			SerializationUtils.serializeRawdata(_out, data);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			sys_src = SerializationUtils.deserializePlaintext(buf);
			sys_dst = SerializationUtils.deserializePlaintext(buf);
			long flags_val = buf.get() & 0xFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
			data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FLAGS {
		URF_PROMISCUOUS(0x01l),

		URF_DELAYED(0x02l);

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
