package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

public class HistoricDataQuery extends Message {
	public static final int ID_STATIC = 187;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int max_size = 0;

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public HistoricData data = null;

	public int mgid() {
		return 187;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeShort(max_size);
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
			max_size = buf.getShort() & 0xFFFF;
			data = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		HRTYPE_QUERY(1l),

		HRTYPE_REPLY(2l),

		HRTYPE_CLEAR(3l);

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
