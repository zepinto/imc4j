package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * This message will hold a set of plans that need to be executed at specified times.
 */
public class TemporalPlan extends Message {
	public static final int ID_STATIC = 910;

	/**
	 * The unique identifier for this plan.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	/**
	 * Temporal actions contained in the plan.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<TemporalAction> actions = new ArrayList<>();

	/**
	 * The unique identifier for this plan.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<VehicleDepot> depots = new ArrayList<>();

	public String abbrev() {
		return "TemporalPlan";
	}

	public int mgid() {
		return 910;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, plan_id);
			SerializationUtils.serializeMsgList(_out, actions);
			SerializationUtils.serializeMsgList(_out, depots);
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
			actions = SerializationUtils.deserializeMsgList(buf);
			depots = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
