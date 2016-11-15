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
 * This message is used to control TREX execution
 */
public class TrexOperation extends Message {
	public static final int ID_STATIC = 655;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * The id of the goal, if applicable (OP == POST_GOAL || OP == RECALL_GOAL)
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String goal_id = "";

	/**
	 * Goal / observation to post, if applicable (OP == POST_GOAL || OP == POST_TOKEN)
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public TrexToken token = null;

	public int mgid() {
		return 655;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)op.value());
			SerializationUtils.serializePlaintext(_out, goal_id);
			SerializationUtils.serializeInlineMsg(_out, token);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			op = OP.valueOf(buf.get() & 0xFF);
			goal_id = SerializationUtils.deserializePlaintext(buf);
			token = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		OP_POST_TOKEN(1l),

		OP_POST_GOAL(2l),

		OP_RECALL_GOAL(3l),

		OP_REQUEST_PLAN(4l),

		OP_REPORT_PLAN(5l);

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
