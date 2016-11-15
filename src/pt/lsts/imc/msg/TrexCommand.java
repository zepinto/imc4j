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
public class TrexCommand extends Message {
	public static final int ID_STATIC = 652;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMMAND command = COMMAND.values()[0];

	/**
	 * The id of the goal, if applicable (OP == POST_GOAL || OP == RECALL_GOAL)
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String goal_id = "";

	/**
	 * The goal encoded as XML, if applicable (OP == POST_GOAL)
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String goal_xml = "";

	public int mgid() {
		return 652;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)command.value());
			SerializationUtils.serializePlaintext(_out, goal_id);
			SerializationUtils.serializePlaintext(_out, goal_xml);
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
			goal_id = SerializationUtils.deserializePlaintext(buf);
			goal_xml = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum COMMAND {
		OP_DISABLE(0l),

		OP_ENABLE(1l),

		OP_POST_GOAL(2l),

		OP_RECALL_GOAL(3l),

		OP_REQUEST_PLAN(4l),

		OP_REPORT_PLAN(5l);

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
