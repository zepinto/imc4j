package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * This message is sent by the TREX task which gives further information to a TREX instance about connected IMC nodes
 */
public class VehicleLinks extends Message {
	public static final int ID_STATIC = 650;

	/**
	 * The name of the vehicle being controlled
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String localname = "";

	/**
	 * A list of Announce messages with last announces heard
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<Announce> links = new ArrayList<>();

	public String abbrev() {
		return "VehicleLinks";
	}

	public int mgid() {
		return 650;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, localname);
			SerializationUtils.serializeMsgList(_out, links);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			localname = SerializationUtils.deserializePlaintext(buf);
			links = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
