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

public class EntityMonitoringState extends Message {
	public static final int ID_STATIC = 503;

	/**
	 * Number of entitities being monitored.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int mcount = 0;

	/**
	 * Comma separated list of all entity names being monitored.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String mnames = "";

	/**
	 * Number of entitities with non-critical errors.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int ecount = 0;

	/**
	 * Comma separated list of all entity names with non-critical
	 * errors.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String enames = "";

	/**
	 * Number of entitities with critical errors.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int ccount = 0;

	/**
	 * Comma separated list of all entity names with critical errors.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String cnames = "";

	/**
	 * Description of last error.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String last_error = "";

	/**
	 * Time of last error (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double last_error_time = 0;

	public String abbrev() {
		return "EntityMonitoringState";
	}

	public int mgid() {
		return 503;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(mcount);
			SerializationUtils.serializePlaintext(_out, mnames);
			_out.writeByte(ecount);
			SerializationUtils.serializePlaintext(_out, enames);
			_out.writeByte(ccount);
			SerializationUtils.serializePlaintext(_out, cnames);
			SerializationUtils.serializePlaintext(_out, last_error);
			_out.writeDouble(last_error_time);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			mcount = buf.get() & 0xFF;
			mnames = SerializationUtils.deserializePlaintext(buf);
			ecount = buf.get() & 0xFF;
			enames = SerializationUtils.deserializePlaintext(buf);
			ccount = buf.get() & 0xFF;
			cnames = SerializationUtils.deserializePlaintext(buf);
			last_error = SerializationUtils.deserializePlaintext(buf);
			last_error_time = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
