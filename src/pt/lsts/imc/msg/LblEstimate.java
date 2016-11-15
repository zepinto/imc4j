package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * LBL Beacon position estimate.
 */
public class LblEstimate extends Message {
	public static final int ID_STATIC = 360;

	/**
	 * LBL Beacon configuration estimate.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public LblBeacon beacon = null;

	/**
	 * The North position offset of the NED field with respect to origin.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float x = 0f;

	/**
	 * The East position offset of the NED field with respect to origin.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float y = 0f;

	/**
	 * The North offset variance of the North/East/Down
	 * field with respect to LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float var_x = 0f;

	/**
	 * The East offset variance of the North/East/Down
	 * field with respect to LLH.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float var_y = 0f;

	/**
	 * Distance between current LBL Beacon position and filter estimation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float distance = 0f;

	public int mgid() {
		return 360;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializeInlineMsg(_out, beacon);
			_out.writeFloat(x);
			_out.writeFloat(y);
			_out.writeFloat(var_x);
			_out.writeFloat(var_y);
			_out.writeFloat(distance);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			beacon = SerializationUtils.deserializeInlineMsg(buf);
			x = buf.getFloat();
			y = buf.getFloat();
			var_x = buf.getFloat();
			var_y = buf.getFloat();
			distance = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
