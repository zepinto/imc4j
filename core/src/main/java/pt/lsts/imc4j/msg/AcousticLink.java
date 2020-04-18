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
 * This message is used to report the perceived link quality to other
 * acoustic peers.
 */
public class AcousticLink extends Message {
	public static final int ID_STATIC = 214;

	/**
	 * The name of the peer on the other side of this link.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String peer = "";

	/**
	 * RSSI is a signed floating point number. Higher RSSI values correspond to
	 * stronger signals.
	 * The signal strength is acceptable when measured RSSI values lie between
	 * -20 dB and -85 dB.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "dB"
	)
	public float rssi = 0f;

	/**
	 * Signal Integrity value illustrates distortion of the last received
	 * acoustic signal. It is calculated based on cross-correlation
	 * measurements.
	 * Higher *Signal Integrity Level* values correspond to less distorted
	 * signals. An acoustic link is considered weak if the *Signal Integrity
	 * Level* value is less than 100.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int integrity = 0;

	public String abbrev() {
		return "AcousticLink";
	}

	public int mgid() {
		return 214;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, peer);
			_out.writeFloat(rssi);
			_out.writeShort(integrity);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			peer = SerializationUtils.deserializePlaintext(buf);
			rssi = buf.getFloat();
			integrity = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
