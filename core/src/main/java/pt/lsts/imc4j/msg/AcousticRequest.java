package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Request Acoustic sending.
 */
public class AcousticRequest extends Message {
	public static final int ID_STATIC = 215;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	/**
	 * The name of the system where to send this message.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String destination = "";

	/**
	 * Period of time to send message (in seconds).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double timeout = 0;

	/**
	 * The meaning of this field depends on the operation and is
	 * explained in the operation's description.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float range = 0f;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Argument for message send ('MSG') or ('RAW') but in this case expects DevDataBinary message requests.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message msg = null;

	public String abbrev() {
		return "AcousticRequest";
	}

	public int mgid() {
		return 215;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			SerializationUtils.serializePlaintext(_out, destination);
			_out.writeDouble(timeout);
			_out.writeFloat(range);
			_out.writeByte((int)(type != null? type.value() : 0));
			SerializationUtils.serializeInlineMsg(_out, msg);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			req_id = buf.getShort() & 0xFFFF;
			destination = SerializationUtils.deserializePlaintext(buf);
			timeout = buf.getDouble();
			range = buf.getFloat();
			type = TYPE.valueOf(buf.get() & 0xFF);
			msg = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		TYPE_ABORT(0l),

		TYPE_RANGE(1l),

		TYPE_REVERSE_RANGE(2l),

		TYPE_MSG(3l),

		TYPE_RAW(4l);

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
