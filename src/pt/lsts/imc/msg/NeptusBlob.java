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

public class NeptusBlob extends Message {
	public static final int ID_STATIC = 888;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String content_type = "";

	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] content = new byte[0];

	public int mgid() {
		return 888;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, content_type);
			SerializationUtils.serializeRawdata(_out, content);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			content_type = SerializationUtils.deserializePlaintext(buf);
			content = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
