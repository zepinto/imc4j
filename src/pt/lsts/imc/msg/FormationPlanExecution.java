package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * A "Formation Plan" is a maneuver specifying a plan for a team of vehicles.
 * The maneuver defines:
 * - Vehicles included in the formation group
 * - Formation shape configuration
 * - Plan (set of maneuvers) to be followed by the formation center
 * - Speed at which that plan is followed
 * - Path contrains (virtual leader bank limit)
 * - Supervision settings
 */
public class FormationPlanExecution extends Maneuver {
	public static final int ID_STATIC = 477;

	/**
	 * Target group for the formation plan.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String group_name = "";

	/**
	 * Name of the formation configuration.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String formation_name = "";

	/**
	 * The flight plan's identifier.
	 * Flight plan defined to be tracked by the formation leader.
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
	 * Formation leader flight airspeed during the plan tracking.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float leader_speed = 0f;

	/**
	 * Formation leader flight bank limit during the plan tracking.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float leader_bank_lim = 0f;

	/**
	 * Limit for the position mismatch between real and simulated position, before a maneuver abort is asserted.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float pos_sim_err_lim = 0f;

	/**
	 * Warning threshold for the position mismatch between real and simulated position.
	 * Above this threshold a time-out limit is evaluated to assert a maneuver abort state.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float pos_sim_err_wrn = 0f;

	/**
	 * The amount of time the maneuver is allowed to run after the position mismatch threshold is surpassed.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int pos_sim_err_timeout = 0;

	/**
	 * Threshold for the convergence measure, above which a time-out limit
	 * is evaluated to assert a maneuver abort state.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float converg_max = 0f;

	/**
	 * The amount of time the maneuver is allowed to run after the convergence threshold is surpassed.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int converg_timeout = 0;

	/**
	 * The amount of time the maneuver is allowed to run without any update on the other formation vehicles states.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int comms_timeout = 0;

	/**
	 * Turbulence limit above which a maneuver abort is asserted.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float turb_lim = 0f;

	/**
	 * Custom settings for maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public String custom = "";

	public int mgid() {
		return 477;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, group_name);
			SerializationUtils.serializePlaintext(_out, formation_name);
			SerializationUtils.serializePlaintext(_out, plan_id);
			SerializationUtils.serializePlaintext(_out, description);
			_out.writeFloat(leader_speed);
			_out.writeFloat(leader_bank_lim);
			_out.writeFloat(pos_sim_err_lim);
			_out.writeFloat(pos_sim_err_wrn);
			_out.writeShort(pos_sim_err_timeout);
			_out.writeFloat(converg_max);
			_out.writeShort(converg_timeout);
			_out.writeShort(comms_timeout);
			_out.writeFloat(turb_lim);
			SerializationUtils.serializePlaintext(_out, custom);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			group_name = SerializationUtils.deserializePlaintext(buf);
			formation_name = SerializationUtils.deserializePlaintext(buf);
			plan_id = SerializationUtils.deserializePlaintext(buf);
			description = SerializationUtils.deserializePlaintext(buf);
			leader_speed = buf.getFloat();
			leader_bank_lim = buf.getFloat();
			pos_sim_err_lim = buf.getFloat();
			pos_sim_err_wrn = buf.getFloat();
			pos_sim_err_timeout = buf.getShort() & 0xFFFF;
			converg_max = buf.getFloat();
			converg_timeout = buf.getShort() & 0xFFFF;
			comms_timeout = buf.getShort() & 0xFFFF;
			turb_lim = buf.getFloat();
			custom = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
