package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

/**
 * Definition of a vehicle participant in a VehicleFormation maneuver.
 */
public class VehicleFormationParticipant extends Message {
	public static final int ID_STATIC = 467;

	/**
	 * IMC address of vehicle.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int vid = 0;

	/**
	 * Distance that the system must respect along the xx axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float off_x = 0f;

	/**
	 * Distance that the system must respect along the yy axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float off_y = 0f;

	/**
	 * Distance that the system must respect along the zz axis.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float off_z = 0f;

	public String abbrev() {
		return "VehicleFormationParticipant";
	}

	public int mgid() {
		return 467;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(vid);
			_out.writeFloat(off_x);
			_out.writeFloat(off_y);
			_out.writeFloat(off_z);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			vid = buf.getShort() & 0xFFFF;
			off_x = buf.getFloat();
			off_y = buf.getFloat();
			off_z = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
