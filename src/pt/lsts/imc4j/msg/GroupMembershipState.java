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
 * Group communication link assertion.
 */
public class GroupMembershipState extends Message {
	public static final int ID_STATIC = 180;

	/**
	 * Name of the group of systems.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String group_name = "";

	/**
	 * Communication link assertion for each group member.
	 * One bit to assert each system communication link state.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long links = 0;

	public String abbrev() {
		return "GroupMembershipState";
	}

	public int mgid() {
		return 180;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, group_name);
			_out.writeInt((int)links);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			group_name = SerializationUtils.deserializePlaintext(buf);
			links = buf.getInt() & 0xFFFFFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
