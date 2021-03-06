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

public class EmergencyControl extends Message {
	public static final int ID_STATIC = 554;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMMAND command = COMMAND.values()[0];

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public PlanSpecification plan = null;

	public String abbrev() {
		return "EmergencyControl";
	}

	public int mgid() {
		return 554;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(command != null? command.value() : 0));
			SerializationUtils.serializeInlineMsg(_out, plan);
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
			plan = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum COMMAND {
		ECTL_ENABLE(0l),

		ECTL_DISABLE(1l),

		ECTL_START(2l),

		ECTL_STOP(3l),

		ECTL_QUERY(4l),

		ECTL_SET_PLAN(5l);

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
