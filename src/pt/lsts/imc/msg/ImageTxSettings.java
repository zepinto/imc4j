package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 *
 */
public class ImageTxSettings extends Message {
	public static final int ID_STATIC = 703;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int fps = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int quality = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int reps = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int tsize = 0;

	public int mgid() {
		return 703;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(fps);
			_out.writeByte(quality);
			_out.writeByte(reps);
			_out.writeByte(tsize);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			fps = buf.get() & 0xFF;
			quality = buf.get() & 0xFF;
			reps = buf.get() & 0xFF;
			tsize = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
