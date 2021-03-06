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
import pt.lsts.imc4j.util.TupleList;

/**
 * This message is used for signaling asynchronous events between different (sub) systems.
 */
public class Event extends Message {
	public static final int ID_STATIC = 660;

	/**
	 * The name or type of this event
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String topic = "";

	/**
	 * A map with additional event information.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList data = new TupleList("");

	public String abbrev() {
		return "Event";
	}

	public int mgid() {
		return 660;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, topic);
			SerializationUtils.serializePlaintext(_out, data == null? null : data.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			topic = SerializationUtils.deserializePlaintext(buf);
			data = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
