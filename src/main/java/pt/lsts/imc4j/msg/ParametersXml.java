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
 * Message containing the parameters XML of the source system.
 */
public class ParametersXml extends Message {
	public static final int ID_STATIC = 893;

	/**
	 * The locale used to produce this parameters XML.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String locale = "";

	/**
	 * The parameters XML file compressed using the GNU zip (gzip) format.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] config = new byte[0];

	public String abbrev() {
		return "ParametersXml";
	}

	public int mgid() {
		return 893;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, locale);
			SerializationUtils.serializeRawdata(_out, config);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			locale = SerializationUtils.deserializePlaintext(buf);
			config = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
