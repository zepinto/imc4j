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
 * Request/reply to plan database.
 */
public class PlanDB extends Message {
	public static final int ID_STATIC = 556;

	/**
	 * Indicates if the message is a request, or a reply to a
	 * previous request.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Indicates the operation affecting the DB.
	 * The operation may relate to a single plan or the entire plan DB.
	 * For each request,  a plan DB may reply with any number of 'in progress'
	 * replies followed by a success or a failure reply.
	 * The 'op', 'request_id' and 'plan_id' fields of a request will be echoed
	 * in one or more responses to that request.
	 * The operation at stake also determines a certain type of the 'arg' field,
	 * and whether or not the 'plan_id' field needs to be set.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Request ID. This may be used by interfacing modules,
	 * e.g. using sequence counters, to annotate requests and
	 * appropriately identify replies
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int request_id = 0;

	/**
	 * Plan identifier for the operation, if one is required.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	/**
	 * Request or reply argument.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message arg = null;

	/**
	 * Human-readable complementary information. For example this
	 * may be used to detail reasons for failure, or to report
	 * in-progress information.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String info = "";

	public int mgid() {
		return 556;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)type.value());
			_out.writeByte((int)op.value());
			_out.writeShort(request_id);
			SerializationUtils.serializePlaintext(_out, plan_id);
			SerializationUtils.serializeInlineMsg(_out, arg);
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
			op = OP.valueOf(buf.get() & 0xFF);
			request_id = buf.getShort() & 0xFFFF;
			plan_id = SerializationUtils.deserializePlaintext(buf);
			arg = SerializationUtils.deserializeInlineMsg(buf);
			info = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		DBT_REQUEST(0l),

		DBT_SUCCESS(1l),

		DBT_FAILURE(2l),

		DBT_IN_PROGRESS(3l);

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

	public enum OP {
		DBOP_SET(0l),

		DBOP_DEL(1l),

		DBOP_GET(2l),

		DBOP_GET_INFO(3l),

		DBOP_CLEAR(4l),

		DBOP_GET_STATE(5l),

		DBOP_GET_DSTATE(6l),

		DBOP_BOOT(7l);

		protected long value;

		OP(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static OP valueOf(long value) throws IllegalArgumentException {
			for (OP v : OP.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for OP: "+value);
		}
	}
}
