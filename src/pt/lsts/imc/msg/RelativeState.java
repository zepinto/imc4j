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
 * Inter-vehicle formation state.
 */
public class RelativeState extends Message {
	public static final int ID_STATIC = 482;

	/**
	 * The identifier of the vehicle whose relative state is being reported.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String s_id = "";

	/**
	 * Distance between vehicles.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float dist = 0f;

	/**
	 * Relative position error norm.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float err = 0f;

	/**
	 * Weight in the computation of the desired acceleration.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ctrl_imp = 0f;

	/**
	 * Inter-vehicle direction vector: North component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rel_dir_x = 0f;

	/**
	 * Inter-vehicle direction vector: East component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rel_dir_y = 0f;

	/**
	 * Inter-vehicle direction vector: Down component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rel_dir_z = 0f;

	/**
	 * Relative position error: North component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float err_x = 0f;

	/**
	 * Relative position error: East component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float err_y = 0f;

	/**
	 * Relative position error: Down component.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float err_z = 0f;

	/**
	 * Relative position error: X component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rf_err_x = 0f;

	/**
	 * Relative position error: Y component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rf_err_y = 0f;

	/**
	 * Relative position error: Z component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rf_err_z = 0f;

	/**
	 * Relative veloctity error: X component in the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rf_err_vx = 0f;

	/**
	 * Relative velocity error: Y component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rf_err_vy = 0f;

	/**
	 * Relative velocity error: Z component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float rf_err_vz = 0f;

	/**
	 * Deviation from convergence (sliding surface): X component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ss_x = 0f;

	/**
	 * Deviation from convergence (sliding surface): Y component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ss_y = 0f;

	/**
	 * Deviation from convergence (sliding surface): Z component on the inter-vehicle reference frame.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ss_z = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Relative virtual error: northward direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float virt_err_x = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Relative virtual error: eastward direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float virt_err_y = 0f;

	/**
	 * Components of the vehicle desired acceleration.
	 * Relative virtual error: downward direction.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float virt_err_z = 0f;

	public String abbrev() {
		return "RelativeState";
	}

	public int mgid() {
		return 482;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, s_id);
			_out.writeFloat(dist);
			_out.writeFloat(err);
			_out.writeFloat(ctrl_imp);
			_out.writeFloat(rel_dir_x);
			_out.writeFloat(rel_dir_y);
			_out.writeFloat(rel_dir_z);
			_out.writeFloat(err_x);
			_out.writeFloat(err_y);
			_out.writeFloat(err_z);
			_out.writeFloat(rf_err_x);
			_out.writeFloat(rf_err_y);
			_out.writeFloat(rf_err_z);
			_out.writeFloat(rf_err_vx);
			_out.writeFloat(rf_err_vy);
			_out.writeFloat(rf_err_vz);
			_out.writeFloat(ss_x);
			_out.writeFloat(ss_y);
			_out.writeFloat(ss_z);
			_out.writeFloat(virt_err_x);
			_out.writeFloat(virt_err_y);
			_out.writeFloat(virt_err_z);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			s_id = SerializationUtils.deserializePlaintext(buf);
			dist = buf.getFloat();
			err = buf.getFloat();
			ctrl_imp = buf.getFloat();
			rel_dir_x = buf.getFloat();
			rel_dir_y = buf.getFloat();
			rel_dir_z = buf.getFloat();
			err_x = buf.getFloat();
			err_y = buf.getFloat();
			err_z = buf.getFloat();
			rf_err_x = buf.getFloat();
			rf_err_y = buf.getFloat();
			rf_err_z = buf.getFloat();
			rf_err_vx = buf.getFloat();
			rf_err_vy = buf.getFloat();
			rf_err_vz = buf.getFloat();
			ss_x = buf.getFloat();
			ss_y = buf.getFloat();
			ss_z = buf.getFloat();
			virt_err_x = buf.getFloat();
			virt_err_y = buf.getFloat();
			virt_err_z = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
