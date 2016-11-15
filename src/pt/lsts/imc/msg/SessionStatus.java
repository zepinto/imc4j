package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Message transmitted periodically to inform the state of a communication session
 */
public class SessionStatus extends Message {
	public static final int ID_STATIC = 810;

	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long sessid = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATUS status = STATUS.values()[0];

	public int mgid() {
		return 810;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeInt((int)sessid);
			_out.writeByte((int)(status != null? status.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			sessid = buf.getInt() & 0xFFFFFFFF;
			status = STATUS.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATUS {
		STATUS_ESTABLISHED(1l),

		STATUS_CLOSED(2l);

		protected long value;

		STATUS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATUS valueOf(long value) throws IllegalArgumentException {
			for (STATUS v : STATUS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATUS: "+value);
		}
	}
}
