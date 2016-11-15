package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * The "Formation" is a controller to execute a maneuver with a team of vehicles.
 * It defines the:
 * - Vehicles included in the formation group
 * - Vehicles relative positions inside the formation
 * - Reference frame where the relative positions are defined
 * - Formation shape configuration
 * - Plan (set of maneuvers) to be followed by the formation center
 * - Plan contrains (virtual leader speed and bank limits)
 * - Supervision settings
 * The formation reference frame may be:
 * - Earth Fixed: Where the vehicles relative position do not depend on the followed path.
 * This results in all UAVs following the same path with an offset relative to each other;
 * - Path Fixed:  Where the vehicles relative position depends on the followed path,
 * changing the inter-vehicle offset direction with the path direction.
 * - Path Curved:  Where the vehicles relative position depends on the followed path,
 * changing the inter-vehicle offset direction with the path direction and direction
 * change rate.
 * An offset in the xx axis results in a distance over the curved path line.
 * An offset in the yy axis results in an offset of the vehicle path line relative to the
 * formation center path line.
 */
public class Formation extends Message {
	public static final int ID_STATIC = 484;

	/**
	 * Name of the formation configuration.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String formation_name = "";

	/**
	 * Indicates if the message is a request, or a reply to a previous request.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Operation to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Target group for the formation plan.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String group_name = "";

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
	 * Formation reference frame
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public REFERENCE_FRAME reference_frame = REFERENCE_FRAME.values()[0];

	/**
	 * List of formation participants.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<VehicleFormationParticipant> participants = new ArrayList<>();

	/**
	 * Maximum absolute bank allowed for the formation leader.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float leader_bank_lim = 0f;

	/**
	 * Minimum speed allowed for the formation leader flight.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float leader_speed_min = 0f;

	/**
	 * Maximum speed allowed for the formation leader flight.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float leader_speed_max = 0f;

	/**
	 * Minimum altitude allowed for the formation leader flight.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float leader_alt_min = 0f;

	/**
	 * Maximum altitude allowed for the formation leader flight.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float leader_alt_max = 0f;

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
		return 484;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, formation_name);
			_out.writeByte((int)type.value());
			_out.writeByte((int)op.value());
			SerializationUtils.serializePlaintext(_out, group_name);
			SerializationUtils.serializePlaintext(_out, plan_id);
			SerializationUtils.serializePlaintext(_out, description);
			_out.writeByte((int)reference_frame.value());
			SerializationUtils.serializeMsgList(_out, participants);
			_out.writeFloat(leader_bank_lim);
			_out.writeFloat(leader_speed_min);
			_out.writeFloat(leader_speed_max);
			_out.writeFloat(leader_alt_min);
			_out.writeFloat(leader_alt_max);
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
			formation_name = SerializationUtils.deserializePlaintext(buf);
			type = TYPE.valueOf(buf.get() & 0xFF);
			op = OP.valueOf(buf.get() & 0xFF);
			group_name = SerializationUtils.deserializePlaintext(buf);
			plan_id = SerializationUtils.deserializePlaintext(buf);
			description = SerializationUtils.deserializePlaintext(buf);
			reference_frame = REFERENCE_FRAME.valueOf(buf.get() & 0xFF);
			participants = SerializationUtils.deserializeMsgList(buf);
			leader_bank_lim = buf.getFloat();
			leader_speed_min = buf.getFloat();
			leader_speed_max = buf.getFloat();
			leader_alt_min = buf.getFloat();
			leader_alt_max = buf.getFloat();
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

	public enum TYPE {
		FC_REQUEST(0l),

		FC_REPORT(1l);

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
		OP_START(0l),

		OP_STOP(1l),

		OP_READY(2l),

		OP_EXECUTING(3l),

		OP_FAILURE(4l);

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

	public enum REFERENCE_FRAME {
		OP_EARTH_FIXED(0l),

		OP_PATH_FIXED(1l),

		OP_PATH_CURVED(2l);

		protected long value;

		REFERENCE_FRAME(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static REFERENCE_FRAME valueOf(long value) throws IllegalArgumentException {
			for (REFERENCE_FRAME v : REFERENCE_FRAME.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for REFERENCE_FRAME: "+value);
		}
	}
}
