package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Monitoring variables for the formation state and performance.
 */
public class FormationMonitor extends Message {
	public static final int ID_STATIC = 481;

	/**
	 * Commanded acceleration computed by the formation controller: northward direction.
	 * On the vehicle directional reference frame.
	 * Constrained by the vehicle operational limits.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ax_cmd = 0f;

	/**
	 * Commanded acceleration computed by the formation controller: eastward direction.
	 * On the vehicle directional reference frame.
	 * Constrained by the vehicle operational limits.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ay_cmd = 0f;

	/**
	 * Commanded acceleration computed by the formation controller: downward direction.
	 * On the vehicle directional reference frame.
	 * Constrained by the vehicle operational limits.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float az_cmd = 0f;

	/**
	 * Desired acceleration computed by the formation controller: northward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ax_des = 0f;

	/**
	 * Desired acceleration computed by the formation controller: eastward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ay_des = 0f;

	/**
	 * Desired acceleration computed by the formation controller: downward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float az_des = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Overall formation combined virtual error: northward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float virt_err_x = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Overall formation combined virtual error: eastward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float virt_err_y = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Overall formation combined virtual error: downward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float virt_err_z = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Overall formation combined sliding surface feedback: northward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float surf_fdbk_x = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Overall formation combined sliding surface feedback: eastward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float surf_fdbk_y = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Overall formation combined sliding surface feedback: downward direction.
	 * On the fixed reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float surf_fdbk_z = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Dynamics uncertainty compensation: northward direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float surf_unkn_x = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Dynamics uncertainty compensation: eastward direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float surf_unkn_y = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Dynamics uncertainty compensation: downward direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float surf_unkn_z = 0f;

	/**
	 * Combined deviation from convergence (sliding surface): North component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ss_x = 0f;

	/**
	 * Combined deviation from convergence (sliding surface): East component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ss_y = 0f;

	/**
	 * Combined deviation from convergence (sliding surface): Down component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ss_z = 0f;

	/**
	 * List of RelativeState messages, encoding the inter-vehicle formation state.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<RelativeState> rel_state = new ArrayList<>();

	public int mgid() {
		return 481;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(ax_cmd);
			_out.writeFloat(ay_cmd);
			_out.writeFloat(az_cmd);
			_out.writeFloat(ax_des);
			_out.writeFloat(ay_des);
			_out.writeFloat(az_des);
			_out.writeFloat(virt_err_x);
			_out.writeFloat(virt_err_y);
			_out.writeFloat(virt_err_z);
			_out.writeFloat(surf_fdbk_x);
			_out.writeFloat(surf_fdbk_y);
			_out.writeFloat(surf_fdbk_z);
			_out.writeFloat(surf_unkn_x);
			_out.writeFloat(surf_unkn_y);
			_out.writeFloat(surf_unkn_z);
			_out.writeFloat(ss_x);
			_out.writeFloat(ss_y);
			_out.writeFloat(ss_z);
			SerializationUtils.serializeMsgList(_out, rel_state);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			ax_cmd = buf.getFloat();
			ay_cmd = buf.getFloat();
			az_cmd = buf.getFloat();
			ax_des = buf.getFloat();
			ay_des = buf.getFloat();
			az_des = buf.getFloat();
			virt_err_x = buf.getFloat();
			virt_err_y = buf.getFloat();
			virt_err_z = buf.getFloat();
			surf_fdbk_x = buf.getFloat();
			surf_fdbk_y = buf.getFloat();
			surf_fdbk_z = buf.getFloat();
			surf_unkn_x = buf.getFloat();
			surf_unkn_y = buf.getFloat();
			surf_unkn_z = buf.getFloat();
			ss_x = buf.getFloat();
			ss_y = buf.getFloat();
			ss_z = buf.getFloat();
			rel_state = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
