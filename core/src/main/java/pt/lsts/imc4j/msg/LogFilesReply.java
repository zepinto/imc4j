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
 * Files Data Reply.
 */
public class LogFilesReply extends Message {
	public static final int ID_STATIC = 911;

	/**
	 * Associated Request Id
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	/**
	 * Associated Request Type
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public FileFragment data = null;

	public String abbrev() {
		return "LogFilesReply";
	}

	public int mgid() {
		return 911;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			_out.writeByte((int)(type != null? type.value() : 0));
			SerializationUtils.serializeInlineMsg(_out, data);
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
			type = TYPE.valueOf(buf.get() & 0xFF);
			data = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		LFRTYPE_FETCH(0l),

		LFRTYPE_QUERY(1l),

		LFRTYPE_CLEAR(2l),

		LFRTYPE_CANCEL(3l);

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
