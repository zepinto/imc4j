package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Controls monitoring of entity states in the vehicle.
 */
public class MonitorEntityState extends Message {
	public static final int ID_STATIC = 502;

	/**
	 * Command.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMMAND command = COMMAND.values()[0];

	/**
	 * Comma separated list of entity names.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String entities = "";

	public int mgid() {
		return 502;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)command.value());
			SerializationUtils.serializePlaintext(_out, entities);
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
			entities = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum COMMAND {
		MES_RESET(0l),

		MES_ENABLE(1l),

		MES_DISABLE(2l),

		MES_ENABLE_EXCLUSIVE(3l),

		MES_STATUS(4l);

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
