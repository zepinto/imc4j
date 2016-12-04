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

public class EmergencyControlState extends Message {
	public static final int ID_STATIC = 555;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 100,
			units = "%"
	)
	public int comm_level = 0;

	public String abbrev() {
		return "EmergencyControlState";
	}

	public int mgid() {
		return 555;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(state != null? state.value() : 0));
			SerializationUtils.serializePlaintext(_out, plan_id);
			_out.writeByte(comm_level);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			state = STATE.valueOf(buf.get() & 0xFF);
			plan_id = SerializationUtils.deserializePlaintext(buf);
			comm_level = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		ECS_NOT_CONFIGURED(0l),

		ECS_DISABLED(1l),

		ECS_ENABLED(2l),

		ECS_ARMED(3l),

		ECS_ACTIVE(4l),

		ECS_STOPPING(5l);

		protected long value;

		STATE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATE valueOf(long value) throws IllegalArgumentException {
			for (STATE v : STATE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATE: "+value);
		}
	}
}
