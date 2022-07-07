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

/**
 * Files Query Operations.
 */
public class LogFilesQuery extends Message {
	public static final int ID_STATIC = 910;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Interval Beginning in UTC
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long init = 0;

	/**
	 * Interval End in UTC
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long end = 0;

	public String abbrev() {
		return "LogFilesQuery";
	}

	public int mgid() {
		return 910;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeInt((int)init);
			_out.writeInt((int)end);
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
			init = buf.getInt() & 0xFFFFFFFF;
			end = buf.getInt() & 0xFFFFFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		LFQTYPE_FETCH(0l),

		LFQTYPE_QUERY(1l),

		LFQTYPE_CLEAR(2l),

		LFQTYPE_CANCEL(3l);

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
