package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Named plan maneuver.
 */
public class PlanManeuver extends Message {
	public static final int ID_STATIC = 552;

	/**
	 * The maneuver ID.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String maneuver_id = "";

	/**
	 * The maneuver specification.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Maneuver data = null;

	/**
	 * Contains an optionally defined 'MessageList' for actions fired
	 * on plan activation.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<Message> start_actions = new ArrayList<>();

	/**
	 * Contains an optionally defined 'MessageList' for actions fired
	 * on plan termination.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<Message> end_actions = new ArrayList<>();

	public int mgid() {
		return 552;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, maneuver_id);
			SerializationUtils.serializeInlineMsg(_out, data);
			SerializationUtils.serializeMsgList(_out, start_actions);
			SerializationUtils.serializeMsgList(_out, end_actions);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			maneuver_id = SerializationUtils.deserializePlaintext(buf);
			data = SerializationUtils.deserializeInlineMsg(buf);
			start_actions = SerializationUtils.deserializeMsgList(buf);
			end_actions = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
