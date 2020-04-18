package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Plan control request/reply.
 */
public class PlanControl extends Message {
	public static final int ID_STATIC = 559;

	/**
	 * Indicates if the message is a request or a reply to a
	 * previous request. The *op*, *request_id* and *plan_id* fields
	 * of a request will be echoed in one or more responses to that
	 * request.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Plan control operation.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Request ID. This may be used by interfacing modules e.g. using
	 * sequence counters.  to annotate requests and appropriately
	 * identify replies.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int request_id = 0;

	/**
	 * The identifier for the plan to be stopped / started / loaded /
	 * retrieved according to the command requested (*op* field).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "Bitfield"
	)
	public EnumSet<FLAGS> flags = EnumSet.noneOf(FLAGS.class);

	/**
	 * Complementary message argument for requests/replies.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message arg = null;

	/**
	 * Complementary human-readable information. This is used
	 * in association to replies.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String info = "";

	public String abbrev() {
		return "PlanControl";
	}

	public int mgid() {
		return 559;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeByte((int)(op != null? op.value() : 0));
			_out.writeShort(request_id);
			SerializationUtils.serializePlaintext(_out, plan_id);
			long _flags = 0;
			if (flags != null) {
				for (FLAGS __flags : flags.toArray(new FLAGS[0])) {
					_flags += __flags.value();
				}
			}
			_out.writeShort((int)_flags);
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
			long flags_val = buf.getShort() & 0xFFFF;
			flags.clear();
			for (FLAGS FLAGS_op : FLAGS.values()) {
				if ((flags_val & FLAGS_op.value()) == FLAGS_op.value()) {
					flags.add(FLAGS_op);
				}
			}
			arg = SerializationUtils.deserializeInlineMsg(buf);
			info = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		PC_REQUEST(0l),

		PC_SUCCESS(1l),

		PC_FAILURE(2l),

		PC_IN_PROGRESS(3l);

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
		PC_START(0l),

		PC_STOP(1l),

		PC_LOAD(2l),

		PC_GET(3l);

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

	public enum FLAGS {
		FLG_CALIBRATE(0x0001l),

		FLG_IGNORE_ERRORS(0x0002l);

		protected long value;

		FLAGS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static FLAGS valueOf(long value) throws IllegalArgumentException {
			for (FLAGS v : FLAGS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for FLAGS: "+value);
		}
	}
}
