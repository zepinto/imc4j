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

/**
 * This message describes an entity.
 */
public class EntityInfo extends Message {
	public static final int ID_STATIC = 3;

	/**
	 * Entity identifier.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int id = 0;

	/**
	 * Entity label or empty if the entity id is not valid.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String label = "";

	/**
	 * Name of the plugin/component/subsystem associated with this
	 * entity.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String component = "";

	/**
	 * Amount of time needed to properly activate the entity.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int act_time = 0;

	/**
	 * Amount of time needed to properly deactivate the entity.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int deact_time = 0;

	public String abbrev() {
		return "EntityInfo";
	}

	public int mgid() {
		return 3;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(id);
			SerializationUtils.serializePlaintext(_out, label);
			SerializationUtils.serializePlaintext(_out, component);
			_out.writeShort(act_time);
			_out.writeShort(deact_time);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			id = buf.get() & 0xFF;
			label = SerializationUtils.deserializePlaintext(buf);
			component = SerializationUtils.deserializePlaintext(buf);
			act_time = buf.getShort() & 0xFFFF;
			deact_time = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
