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
import pt.lsts.imc4j.util.TupleList;

public class SoiCommand extends Message {
	public static final int ID_STATIC = 852;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMMAND command = COMMAND.values()[0];

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList settings = new TupleList("");

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public SoiPlan plan = null;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String info = "";

	public String abbrev() {
		return "SoiCommand";
	}

	public int mgid() {
		return 852;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeByte((int)(command != null? command.value() : 0));
			SerializationUtils.serializePlaintext(_out, settings == null? null : settings.toString());
			SerializationUtils.serializeInlineMsg(_out, plan);
			SerializationUtils.serializePlaintext(_out, info);
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
			command = COMMAND.valueOf(buf.get() & 0xFF);
			settings = new TupleList(SerializationUtils.deserializePlaintext(buf));
			plan = SerializationUtils.deserializeInlineMsg(buf);
			info = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		SOITYPE_REQUEST(1l),

		SOITYPE_SUCCESS(2l),

		SOITYPE_ERROR(3l);

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

	public enum COMMAND {
		SOICMD_EXEC(1l),

		SOICMD_STOP(2l),

		SOICMD_SET_PARAMS(3l),

		SOICMD_GET_PARAMS(4l),

		SOICMD_GET_PLAN(5l),

		SOICMD_RESUME(6l);

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
