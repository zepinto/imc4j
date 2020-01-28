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
 * Send an acoustic message.
 */
public class SimAcousticMessage extends Message {
	public static final int ID_STATIC = 207;

	/**
	 * Absolute latitude of sending vehicle.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double lat = 0;

	/**
	 * Absolute longitude of sending vehicle.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double lon = 0;

	/**
	 * Depth of sending vehicle.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float depth = 0f;

	/**
	 * Sentence string sent/received by the modem
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sentence = "";

	/**
	 * Transmission time.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double txtime = 0;

	/**
	 * The modem being used.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String modem_type = "";

	/**
	 * Name of source system.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_src = "";

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int seq = 0;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_dst = "";

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "SimAcousticMessage";
	}

	public int mgid() {
		return 207;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(depth);
			SerializationUtils.serializePlaintext(_out, sentence);
			_out.writeDouble(txtime);
			SerializationUtils.serializePlaintext(_out, modem_type);
			SerializationUtils.serializePlaintext(_out, sys_src);
			_out.writeShort(seq);
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
			lat = buf.getDouble();
			lon = buf.getDouble();
			depth = buf.getFloat();
			sentence = SerializationUtils.deserializePlaintext(buf);
			txtime = buf.getDouble();
			modem_type = SerializationUtils.deserializePlaintext(buf);
			sys_src = SerializationUtils.deserializePlaintext(buf);
			seq = buf.getShort() & 0xFFFF;
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
		SAM_ACK(0x01l),

		SAM_DELAYED(0x02l),

		SAM_REPLY(0x03l);

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
