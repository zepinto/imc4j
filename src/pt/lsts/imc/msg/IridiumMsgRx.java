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

public class IridiumMsgRx extends Message {
	public static final int ID_STATIC = 170;

	/**
	 * The unique identifier of this message's origin device (e.g. lauv-xtreme-2, manta-0).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String origin = "";

	/**
	 * Timestamp (Epoch time).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double htime = 0;

	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lat = 0;

	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Message data.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "IridiumMsgRx";
	}

	public int mgid() {
		return 170;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, origin);
			_out.writeDouble(htime);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			SerializationUtils.serializeRawdata(_out, data);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			origin = SerializationUtils.deserializePlaintext(buf);
			htime = buf.getDouble();
			lat = buf.getDouble();
			lon = buf.getDouble();
			data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
