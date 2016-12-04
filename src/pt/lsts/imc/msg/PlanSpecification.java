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
 * Identity and description of a plan's general parameters,
 * associated with plan loading (i.e. load plan command in
 * *PlanCommand*).
 * A plan specification is defined by a plan identifier, a set of
 * maneuver specifications and a start maneuver from that set.
 * See the :ref:`PlanManeuver` message for details on maneuver
 * specification.
 */
public class PlanSpecification extends Message {
	public static final int ID_STATIC = 551;

	/**
	 * The plan's identifier.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	/**
	 * Verbose text description of plan.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String description = "";

	/**
	 * Namespace for plan variables.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String vnamespace = "";

	/**
	 * Plan variables.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<PlanVariable> variables = new ArrayList<>();

	/**
	 * Indicates the id of the starting maneuver for this plan.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String start_man_id = "";

	/**
	 * List of maneuver specifications.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<PlanManeuver> maneuvers = new ArrayList<>();

	/**
	 * List of maneuver specifications.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<PlanTransition> transitions = new ArrayList<>();

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

	public String abbrev() {
		return "PlanSpecification";
	}

	public int mgid() {
		return 551;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, plan_id);
			SerializationUtils.serializePlaintext(_out, description);
			SerializationUtils.serializePlaintext(_out, vnamespace);
			SerializationUtils.serializeMsgList(_out, variables);
			SerializationUtils.serializePlaintext(_out, start_man_id);
			SerializationUtils.serializeMsgList(_out, maneuvers);
			SerializationUtils.serializeMsgList(_out, transitions);
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
			plan_id = SerializationUtils.deserializePlaintext(buf);
			description = SerializationUtils.deserializePlaintext(buf);
			vnamespace = SerializationUtils.deserializePlaintext(buf);
			variables = SerializationUtils.deserializeMsgList(buf);
			start_man_id = SerializationUtils.deserializePlaintext(buf);
			maneuvers = SerializationUtils.deserializeMsgList(buf);
			transitions = SerializationUtils.deserializeMsgList(buf);
			start_actions = SerializationUtils.deserializeMsgList(buf);
			end_actions = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
