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
 * This message is used to signal events among running CCUs.
 */
public class CcuEvent extends Message {
	public static final int ID_STATIC = 606;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String id = "";

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message arg = null;

	public String abbrev() {
		return "CcuEvent";
	}

	public int mgid() {
		return 606;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			SerializationUtils.serializePlaintext(_out, id);
			SerializationUtils.serializeInlineMsg(_out, arg);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			type = TYPE.valueOf(buf.get() & 0xFF);
			id = SerializationUtils.deserializePlaintext(buf);
			arg = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		EVT_LOG_ENTRY(1l),

		EVT_PLAN_ADDED(2l),

		EVT_PLAN_REMOVED(3l),

		EVT_PLAN_CHANGED(4l),

		EVT_MAP_FEATURE_ADDED(5l),

		EVT_MAP_FEATURE_REMOVED(6l),

		EVT_MAP_FEATURE_CHANGED(7l),

		EVT_TELEOPERATION_STARTED(8l),

		EVT_TELEOPERATION_ENDED(9l);

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
