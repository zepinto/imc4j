package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * This message is used to store historic (transmitted afterwards) sonar data.
 */
public class HistoricSonarData extends Message {
	public static final int ID_STATIC = 109;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float altitude = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float width = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float length = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float bearing = 0f;

	/**
	 * The number of pixels per line as the data in 'sonar_data' may
	 * correspond to more than one sequential sidescan lines.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16
	)
	public int pxl = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ENCODING encoding = ENCODING.values()[0];

	/**
	 * Sonar data encoded as in 'encoding'.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] sonar_data = new byte[0];

	public int mgid() {
		return 109;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(altitude);
			_out.writeFloat(width);
			_out.writeFloat(length);
			_out.writeFloat(bearing);
			_out.writeShort(pxl);
			_out.writeByte((int)(encoding != null? encoding.value() : 0));
			SerializationUtils.serializeRawdata(_out, sonar_data);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			altitude = buf.getFloat();
			width = buf.getFloat();
			length = buf.getFloat();
			bearing = buf.getFloat();
			pxl = buf.getShort();
			encoding = ENCODING.valueOf(buf.get() & 0xFF);
			sonar_data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ENCODING {
		ENC_ONE_BYTE_PER_PIXEL(0l),

		ENC_PNG(1l),

		ENC_JPEG(2l);

		protected long value;

		ENCODING(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ENCODING valueOf(long value) throws IllegalArgumentException {
			for (ENCODING v : ENCODING.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ENCODING: "+value);
		}
	}
}
