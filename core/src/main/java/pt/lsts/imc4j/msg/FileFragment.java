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

/**
 * File fragment.
 */
public class FileFragment extends Message {
	public static final int ID_STATIC = 912;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String id = "";

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int frag_number = 0;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int num_frags = 0;

	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "FileFragment";
	}

	public int mgid() {
		return 912;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, id);
			_out.writeShort(frag_number);
			_out.writeShort(num_frags);
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
			id = SerializationUtils.deserializePlaintext(buf);
			frag_number = buf.getShort() & 0xFFFF;
			num_frags = buf.getShort() & 0xFFFF;
			data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
