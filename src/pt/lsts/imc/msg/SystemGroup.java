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
 * Group of systems configuration.
 */
public class SystemGroup extends Message {
	public static final int ID_STATIC = 181;

	/**
	 * Name of the group of systems.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String GroupName = "";

	/**
	 * Actions on the group list.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ACTION Action = ACTION.values()[0];

	/**
	 * List of names of system in the group, separated by commas.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String GroupList = "";

	public int mgid() {
		return 181;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, GroupName);
			_out.writeByte((int)(Action != null? Action.value() : 0));
			SerializationUtils.serializePlaintext(_out, GroupList);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			GroupName = SerializationUtils.deserializePlaintext(buf);
			Action = ACTION.valueOf(buf.get() & 0xFF);
			GroupList = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ACTION {
		OP_Dis(0l),

		OP_Set(1l),

		OP_Req(2l),

		OP_Chg(3l),

		OP_Rep(4l),

		OP_Frc(5l);

		protected long value;

		ACTION(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ACTION valueOf(long value) throws IllegalArgumentException {
			for (ACTION v : ACTION.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ACTION: "+value);
		}
	}
}
