package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * This maneuver follows a reference given by an external entity.
 */
public class FollowReference extends Maneuver {
	public static final int ID_STATIC = 478;

	/**
	 * The IMC identifier of the source system that is allowed to provide references to this maneuver.
	 * If the value ''0xFFFF'' is used, any system is allowed to command references.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int control_src = 0;

	/**
	 * The entity identifier of the entity that is allowed to provide references to this maneuver.
	 * If the value ''0xFF'' is used, any entity is allowed to command references.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int control_ent = 0;

	/**
	 * The ammount of time, in seconds, after which the maneuver will be terminated if no reference has
	 * been received. In other words, the controlling entity should send reference updates in shorter periods than
	 * 'timeout'.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float timeout = 0f;

	/**
	 * Whenever an intended reference is achieved, this maneuver will maintain the vehicle in vaticiny of that
	 * location. The loiter radius is used to define the radius of this (xy) area.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float loiter_radius = 0f;

	/**
	 * Similarly to Loiter Radius, this field is used to define the "z" distance considered to be inside the vacitiny of
	 * the target location. An AUV may, for instance, be floating until it more than z units above the current reference,
	 * in which case it actively changes its position in order to achieve the desired depth / altitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float altitude_interval = 0f;

	public int mgid() {
		return 478;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(control_src);
			_out.writeByte(control_ent);
			_out.writeFloat(timeout);
			_out.writeFloat(loiter_radius);
			_out.writeFloat(altitude_interval);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			control_src = buf.getShort() & 0xFFFF;
			control_ent = buf.get() & 0xFF;
			timeout = buf.getFloat();
			loiter_radius = buf.getFloat();
			altitude_interval = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
