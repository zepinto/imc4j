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
 * Message containing the x and y coordinates of object to track in image slave.
 */
public class GetImageCoords extends Message {
	public static final int ID_STATIC = 896;

	/**
	 * Camera identifier.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int camId = 0;

	/**
	 * X coordinate of the target in the image frame.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "px"
	)
	public int x = 0;

	/**
	 * Y coordinate of the target in the image frame.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "px"
	)
	public int y = 0;

	public String abbrev() {
		return "GetImageCoords";
	}

	public int mgid() {
		return 896;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(camId);
			_out.writeShort(x);
			_out.writeShort(y);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			camId = buf.get() & 0xFF;
			x = buf.getShort() & 0xFFFF;
			y = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
