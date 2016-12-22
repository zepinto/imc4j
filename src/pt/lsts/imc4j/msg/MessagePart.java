package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

public class MessagePart extends Message {
	public static final int ID_STATIC = 877;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int uid = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int frag_number = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int num_frags = 0;

	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "MessagePart";
	}

	public int mgid() {
		return 877;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(uid);
			_out.writeByte(frag_number);
			_out.writeByte(num_frags);
			SerializationUtils.serializeRawdata(_out, data);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			uid = buf.get() & 0xFF;
			frag_number = buf.get() & 0xFF;
			num_frags = buf.get() & 0xFF;
			data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
