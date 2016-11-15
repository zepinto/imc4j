package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Characterizes the state of the entire plan database.
 */
public class PlanDBState extends Message {
	public static final int ID_STATIC = 557;

	/**
	 * Number of stored plans.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int plan_count = 0;

	/**
	 * Size of all plans.The value equals the sum of the IMC payload
	 * sizes for 'PlanSpecification' stored in the DB.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long plan_size = 0;

	/**
	 * Time of last change (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double change_time = 0;

	/**
	 * IMC address for source of last DB change.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int change_sid = 0;

	/**
	 * IMC node name for source of last DB change.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String change_sname = "";

	/**
	 * MD5 database verification code. The MD5 hash sum is computed
	 * over the stream formed by the MD5 of all plans, ordered by
	 * plan id, in compliance with RFC 1321.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] md5 = new byte[0];

	/**
	 * Individual information for plans.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<PlanDBInformation> plans_info = new ArrayList<>();

	public int mgid() {
		return 557;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(plan_count);
			_out.writeInt((int)plan_size);
			_out.writeDouble(change_time);
			_out.writeShort(change_sid);
			SerializationUtils.serializePlaintext(_out, change_sname);
			SerializationUtils.serializeRawdata(_out, md5);
			SerializationUtils.serializeMsgList(_out, plans_info);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			plan_count = buf.getShort() & 0xFFFF;
			plan_size = buf.getInt() & 0xFFFFFFFF;
			change_time = buf.getDouble();
			change_sid = buf.getShort() & 0xFFFF;
			change_sname = SerializationUtils.deserializePlaintext(buf);
			md5 = SerializationUtils.deserializeRawdata(buf);
			plans_info = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
