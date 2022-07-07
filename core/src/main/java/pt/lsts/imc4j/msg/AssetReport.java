package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * This message is represents an Asset position / status.
 */
public class AssetReport extends Message {
	public static final int ID_STATIC = 525;

	/**
	 * The human readable name of the asset.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String name = "";

	/**
	 * Time in seconds since epoch, for the generation instant.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double report_time = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public MEDIUM medium = MEDIUM.values()[0];

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

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float depth = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float alt = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float sog = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float cog = 0f;

	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<Message> msgs = new ArrayList<>();

	public String abbrev() {
		return "AssetReport";
	}

	public int mgid() {
		return 525;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, name);
			_out.writeDouble(report_time);
			_out.writeByte((int)(medium != null? medium.value() : 0));
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(depth);
			_out.writeFloat(alt);
			_out.writeFloat(sog);
			_out.writeFloat(cog);
			SerializationUtils.serializeMsgList(_out, msgs);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			name = SerializationUtils.deserializePlaintext(buf);
			report_time = buf.getDouble();
			medium = MEDIUM.valueOf(buf.get() & 0xFF);
			lat = buf.getDouble();
			lon = buf.getDouble();
			depth = buf.getFloat();
			alt = buf.getFloat();
			sog = buf.getFloat();
			cog = buf.getFloat();
			msgs = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum MEDIUM {
		RM_WIFI(1l),

		RM_SATELLITE(2l),

		RM_ACOUSTIC(3l),

		RM_SMS(4l);

		protected long value;

		MEDIUM(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static MEDIUM valueOf(long value) throws IllegalArgumentException {
			for (MEDIUM v : MEDIUM.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for MEDIUM: "+value);
		}
	}
}
