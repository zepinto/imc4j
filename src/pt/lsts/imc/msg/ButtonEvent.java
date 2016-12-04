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
 * Event of a specific hardware button.
 */
public class ButtonEvent extends Message {
	public static final int ID_STATIC = 306;

	/**
	 * Button identifier.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int button = 0;

	/**
	 * Value of the button.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int value = 0;

	public String abbrev() {
		return "ButtonEvent";
	}

	public int mgid() {
		return 306;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(button);
			_out.writeByte(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			button = buf.get() & 0xFF;
			value = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
