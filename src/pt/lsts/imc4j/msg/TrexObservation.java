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
 * This message is sent to TREX to post timeline observations
 */
public class TrexObservation extends Message {
	public static final int ID_STATIC = 651;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String timeline = "";

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String predicate = "";

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList attributes = new TupleList("");

	public String abbrev() {
		return "TrexObservation";
	}

	public int mgid() {
		return 651;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, timeline);
			SerializationUtils.serializePlaintext(_out, predicate);
			SerializationUtils.serializePlaintext(_out, attributes == null? null : attributes.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			timeline = SerializationUtils.deserializePlaintext(buf);
			predicate = SerializationUtils.deserializePlaintext(buf);
			attributes = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
