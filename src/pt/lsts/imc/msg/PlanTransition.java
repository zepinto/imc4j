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
 * Describes a plan transition within a plan specification. A
 * transition states the vehicle conditions that must be met to
 * signal the transition, the maneuver that should be started as a
 * result, and an optional set of actions triggered by the
 * transition.
 */
public class PlanTransition extends Message {
	public static final int ID_STATIC = 553;

	/**
	 * Comma separated list of maneuver IDs, or the special value '.'
	 * to identify a global plan transition.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String source_man = "";

	/**
	 * Target maneuver name.
	 * If it equals the special value '_done_' then plan should
	 * terminate with a success status.
	 * If it equals the special value '_error_' then the plan should
	 * terminate with an error status.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String dest_man = "";

	/**
	 * Comma separated list of conditions for transition. Each
	 * condition identifier corresponds to a known predicate which is
	 * interpreted and tested internally by the vehicle.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String conditions = "";

	/**
	 * Messages processed when the transition is triggered.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<Message> actions = new ArrayList<>();

	public String abbrev() {
		return "PlanTransition";
	}

	public int mgid() {
		return 553;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, source_man);
			SerializationUtils.serializePlaintext(_out, dest_man);
			SerializationUtils.serializePlaintext(_out, conditions);
			SerializationUtils.serializeMsgList(_out, actions);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			source_man = SerializationUtils.deserializePlaintext(buf);
			dest_man = SerializationUtils.deserializePlaintext(buf);
			conditions = SerializationUtils.deserializePlaintext(buf);
			actions = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
