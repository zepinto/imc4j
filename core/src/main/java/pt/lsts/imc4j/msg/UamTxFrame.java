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
 * This message shall be sent to acoustic modem drivers to request
 * transmission of a data frame via the acoustic channel.
 */
public class UamTxFrame extends Message {
	public static final int ID_STATIC = 814;

	/**
	 * A sequence identifier that should be incremented for each
	 * request. This number will then be used to issue transmission
	 * status updates via the message UamTxStatus.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int seq = 0;

	/**
	 * The canonical name of the destination system. If supported, the
	 * special destination 'broadcast' shall be used to dispatch messages
	 * to all nodes.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_dst = "";

	/**
	 * Transmission flags.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	/**
	 * The actual data frame to transmit. The data size shall not exceed
	 * the MTU of the acoustic modem.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "UamTxFrame";
	}

	public int mgid() {
		return 814;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
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
		UTF_ACK(0x01l),

		UTF_DELAYED(0x02l),

		UTF_FORCED(0x04l);

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
