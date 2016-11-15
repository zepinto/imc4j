package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Distance measurement detected by the device.
 */
public class Distance extends Message {
	public static final int ID_STATIC = 262;

	/**
	 * Validity of the measurement.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public VALIDITY validity = VALIDITY.values()[0];

	/**
	 * Device Location in the system.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<DeviceState> location = new ArrayList<>();

	/**
	 * Beam configuration of the device.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<BeamConfig> beam_config = new ArrayList<>();

	/**
	 * Measured distance.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float value = 0f;

	public int mgid() {
		return 262;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(validity != null? validity.value() : 0));
			SerializationUtils.serializeMsgList(_out, location);
			SerializationUtils.serializeMsgList(_out, beam_config);
			_out.writeFloat(value);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			validity = VALIDITY.valueOf(buf.get() & 0xFF);
			location = SerializationUtils.deserializeMsgList(buf);
			beam_config = SerializationUtils.deserializeMsgList(buf);
			value = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum VALIDITY {
		DV_INVALID(0l),

		DV_VALID(1l);

		protected long value;

		VALIDITY(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static VALIDITY valueOf(long value) throws IllegalArgumentException {
			for (VALIDITY v : VALIDITY.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for VALIDITY: "+value);
		}
	}
}
