package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Control history log.
 */
public class LogBookControl extends Message {
	public static final int ID_STATIC = 104;

	/**
	 * Command to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMMAND command = COMMAND.values()[0];

	/**
	 * Timestamp for command (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double htime = 0;

	/**
	 * Argument, currently used only for 'REPLY'.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<LogBookEntry> msg = new ArrayList<>();

	public int mgid() {
		return 104;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(command != null? command.value() : 0));
			_out.writeDouble(htime);
			SerializationUtils.serializeMsgList(_out, msg);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			command = COMMAND.valueOf(buf.get() & 0xFF);
			htime = buf.getDouble();
			msg = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum COMMAND {
		LBC_GET(0l),

		LBC_CLEAR(1l),

		LBC_GET_ERR(2l),

		LBC_REPLY(3l);

		protected long value;

		COMMAND(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static COMMAND valueOf(long value) throws IllegalArgumentException {
			for (COMMAND v : COMMAND.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for COMMAND: "+value);
		}
	}
}
