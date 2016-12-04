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

public class UamRxRange extends Message {
	public static final int ID_STATIC = 817;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int seq = 0;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys = "";

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float value = 0f;

	public String abbrev() {
		return "UamRxRange";
	}

	public int mgid() {
		return 817;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(seq);
			SerializationUtils.serializePlaintext(_out, sys);
			_out.writeFloat(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			seq = buf.getShort() & 0xFFFF;
			sys = SerializationUtils.deserializePlaintext(buf);
			value = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
