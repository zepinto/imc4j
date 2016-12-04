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

public class PlanDBInformation extends Message {
	public static final int ID_STATIC = 558;

	/**
	 * Plan identifier.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	/**
	 * Plan size. The value equals the IMC message payload of the
	 * associated 'PlanSpecification' message in bytes.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int plan_size = 0;

	/**
	 * Time of last change to the plan (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double change_time = 0;

	/**
	 * IMC address for source of last change to the plan.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int change_sid = 0;

	/**
	 * IMC node name for source of last change to the plan.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String change_sname = "";

	/**
	 * MD5 plan verification code. The value is calculated over the
	 * message payload of the 'PlanSpecification', in compliance with
	 * RFC 1321.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] md5 = new byte[0];

	public String abbrev() {
		return "PlanDBInformation";
	}

	public int mgid() {
		return 558;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, plan_id);
			_out.writeShort(plan_size);
			_out.writeDouble(change_time);
			_out.writeShort(change_sid);
			SerializationUtils.serializePlaintext(_out, change_sname);
			SerializationUtils.serializeRawdata(_out, md5);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			plan_id = SerializationUtils.deserializePlaintext(buf);
			plan_size = buf.getShort() & 0xFFFF;
			change_time = buf.getDouble();
			change_sid = buf.getShort() & 0xFFFF;
			change_sname = SerializationUtils.deserializePlaintext(buf);
			md5 = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
