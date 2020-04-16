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
 * This message is used to store historic profiles for water parameters: Temperature, Salinity, Chlorophyll...
 */
public class VerticalProfile extends Message {
	public static final int ID_STATIC = 111;

	/**
	 * Water parameter used to calculate the vertical profile.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public PARAMETER parameter = PARAMETER.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int numSamples = 0;

	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<ProfileSample> samples = new ArrayList<>();

	/**
	 * Latitude where the profile was calculated.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * Longitude where the profile was calculated.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lon = 0;

	public String abbrev() {
		return "VerticalProfile";
	}

	public int mgid() {
		return 111;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(parameter != null? parameter.value() : 0));
			_out.writeByte(numSamples);
			SerializationUtils.serializeMsgList(_out, samples);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			parameter = PARAMETER.valueOf(buf.get() & 0xFF);
			numSamples = buf.get() & 0xFF;
			samples = SerializationUtils.deserializeMsgList(buf);
			lat = buf.getDouble();
			lon = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum PARAMETER {
		PROF_TEMPERATURE(0l),

		PROF_SALINITY(1l),

		PROF_CONDUCTIVITY(2l),

		PROF_PH(3l),

		PROF_REDOX(4l),

		PROF_CHLOROPHYLL(5l),

		PROF_TURBIDITY(6l);

		protected long value;

		PARAMETER(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static PARAMETER valueOf(long value) throws IllegalArgumentException {
			for (PARAMETER v : PARAMETER.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for PARAMETER: "+value);
		}
	}
}
