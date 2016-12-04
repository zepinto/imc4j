package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * This message contains the data acquired by a single sonar
 * measurement.
 */
public class SonarData extends Message {
	public static final int ID_STATIC = 276;

	/**
	 * Type of sonar.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Operating frequency.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "Hz"
	)
	public long frequency = 0;

	/**
	 * Minimum range.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "m"
	)
	public int min_range = 0;

	/**
	 * Maximum range.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "m"
	)
	public int max_range = 0;

	/**
	 * Size of the data unit.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "bit"
	)
	public int bits_per_point = 0;

	/**
	 * Scaling factor used to multiply each data unit to restore the
	 * original floating point value.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float scale_factor = 0f;

	/**
	 * Beam configuration of the device.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<BeamConfig> beam_config = new ArrayList<>();

	/**
	 * Data acquired by the measurement.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "SonarData";
	}

	public int mgid() {
		return 276;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeInt((int)frequency);
			_out.writeShort(min_range);
			_out.writeShort(max_range);
			_out.writeByte(bits_per_point);
			_out.writeFloat(scale_factor);
			SerializationUtils.serializeMsgList(_out, beam_config);
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
			type = TYPE.valueOf(buf.get() & 0xFF);
			frequency = buf.getInt() & 0xFFFFFFFF;
			min_range = buf.getShort() & 0xFFFF;
			max_range = buf.getShort() & 0xFFFF;
			bits_per_point = buf.get() & 0xFF;
			scale_factor = buf.getFloat();
			beam_config = SerializationUtils.deserializeMsgList(buf);
			data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		ST_SIDESCAN(0l),

		ST_ECHOSOUNDER(1l),

		ST_MULTIBEAM(2l);

		protected long value;

		TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static TYPE valueOf(long value) throws IllegalArgumentException {
			for (TYPE v : TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for TYPE: "+value);
		}
	}
}
