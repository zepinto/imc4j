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
 * This message represents a simple map that is transferred between CCU consoles (from Neptus to ACCU)
 */
public class Map extends Message {
	public static final int ID_STATIC = 602;

	/**
	 * The id of the map
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String id = "";

	/**
	 * A list of map features.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<MapFeature> features = new ArrayList<>();

	public String abbrev() {
		return "Map";
	}

	public int mgid() {
		return 602;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, id);
			SerializationUtils.serializeMsgList(_out, features);
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
			features = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
