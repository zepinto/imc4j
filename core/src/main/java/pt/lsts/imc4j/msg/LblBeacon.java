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
 * Position and configuration of an LBL transponder (beacon).
 */
public class LblBeacon extends Message {
	public static final int ID_STATIC = 202;

	/**
	 * Name/Label of the acoustic transponder.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String beacon = "";

	/**
	 * WGS-84 Latitude coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * The beacon's depth.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float depth = 0f;

	/**
	 * Interrogation channel.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int query_channel = 0;

	/**
	 * Reply channel.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int reply_channel = 0;

	/**
	 * Transponder delay.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "ms"
	)
	public int transponder_delay = 0;

	public String abbrev() {
		return "LblBeacon";
	}

	public int mgid() {
		return 202;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, beacon);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(depth);
			_out.writeByte(query_channel);
			_out.writeByte(reply_channel);
			_out.writeByte(transponder_delay);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			beacon = SerializationUtils.deserializePlaintext(buf);
			lat = buf.getDouble();
			lon = buf.getDouble();
			depth = buf.getFloat();
			query_channel = buf.get() & 0xFF;
			reply_channel = buf.get() & 0xFF;
			transponder_delay = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
