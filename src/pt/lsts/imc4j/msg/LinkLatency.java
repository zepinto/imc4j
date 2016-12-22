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
 * Communications latency between two systems.
 */
public class LinkLatency extends Message {
	public static final int ID_STATIC = 182;

	/**
	 * Time taken between the communications package/message is sent
	 * from the source until it arrives to the destination.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "s"
	)
	public float value = 0f;

	/**
	 * ID of system that was the source of the communication package.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int sys_src = 0;

	public String abbrev() {
		return "LinkLatency";
	}

	public int mgid() {
		return 182;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(value);
			_out.writeShort(sys_src);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			value = buf.getFloat();
			sys_src = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
