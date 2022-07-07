package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

/**
 * Measurement from one specific beam at the given CellPosition.
 * Water Velocity is provided in the chosen Coordinate system.
 * Amplitude and Correlation are always in the BEAM coordinate system.
 */
public class ADCPBeam extends Message {
	public static final int ID_STATIC = 1016;

	/**
	 * Water velocity measured in the chosen coordinate system.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float vel = 0f;

	/**
	 * Amplitude of returning ping for the beam.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "dB"
	)
	public float amp = 0f;

	/**
	 * Autocorrelation of returning ping for the beam.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			max = 100,
			min = 0,
			units = "%"
	)
	public int cor = 0;

	public String abbrev() {
		return "ADCPBeam";
	}

	public int mgid() {
		return 1016;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(vel);
			_out.writeFloat(amp);
			_out.writeByte(cor);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			vel = buf.getFloat();
			amp = buf.getFloat();
			cor = buf.get() & 0xFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
