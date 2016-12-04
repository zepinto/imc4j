package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Command used to indicate maneuver can be executed in the
 * vehicle.
 */
public class RegisterManeuver extends Message {
	public static final int ID_STATIC = 469;

	/**
	 * IMC serialization ID of maneuver type.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int mid = 0;

	public String abbrev() {
		return "RegisterManeuver";
	}

	public int mgid() {
		return 469;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(mid);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			mid = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
