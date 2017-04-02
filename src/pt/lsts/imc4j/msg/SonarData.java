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
 * This message contains the data acquired by a single sonar
 * measurement. The following describes the format used to
 * fill the data field used in this message. (Byte order is
 * little endian.)
 * **Sidescan:**
 *
 * +------+-------------------+-----------+
 * | Data | Name              | Type      |
 * +======+===================+===========+
 * | A    | Ranges data       |   uintX_t |
 * +------+-------------------+-----------+
 * .. figure:: ../images/imc_sidescan.png
 * * The type *uintX_t* will depend on the number of bits per unit, and it should be a multiple of 8.
 * * Furthermore, for now, 32 bits is the highest value of bits per unit supported.
 * **Multibeam:**
 *
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | Index| Section| Name                    | Type    | Comments                                                             |
 * +======+========+=========================+=========+======================================================================+
 * | 1    | H1     | Number of points        | uint16_t| Number of data points                                                |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | 2    | H2     | Start angle             | fp32_t  | In radians                                                           |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | 3    | H3     | Flags                   | uint8_t | Refer to next table                                                  |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | 4    | H4 ?   | Angle scale factor      | fp32_t  | Used for angle steps in radians                                      |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | 5    | H5 ?   | Intensities scale factor| fp32_t  |                                                                      |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | 6    | D1 ?   | Angle steps[H1]         | uint16_t| Values in radians                                                    |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | 7    | D2     | Ranges[H1]              | uintX_t | Ranges data points (scale factor from common field "Scaling Factor") |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * | 8    | D3 ?   | Intensities[H1]         | uintX_t | Intensities data points                                              |
 * +------+--------+-------------------------+---------+----------------------------------------------------------------------+
 * +--------+------------------+-----+
 * | Section| Flag Label       | Bit |
 * +========+==================+=====+
 * | H4.1   | Intensities flag | 0   |
 * +--------+------------------+-----+
 * | H4.2   | Angle step flag  | 1   |
 * +--------+------------------+-----+
 *
 * .. figure:: ../images/imc_multibeam.png
 * *Notes:*
 * * Each angle at step *i* can be calculated is defined by:
 * .. code-block:: python
 * angle[i] = H2_start_angle + (32-bit sum of D1_angle_step[0] through D1_angle_step[i]) * H4_scaling_factor
 *
 * * If bit H4.1 is not set then sections H5 and D3 won't exist.
 * * If bit H4.2 is not set then sections H4 and D1 won't exist. In case this bit is set, then the angle steps is read from field "Beam Width" from "Beam Configuration".
 * * The type *uintX_t* will depend on the number of bits per unit, and it should be a multiple of 8.
 * * Furthermore, for now, 32 bits is the highest value of bits per unit supported.
 *
 * *How to write ranges and intensities data:*
 * .. code-block:: python
 * :linenos:
 *
 * data_unit = (Integer) (data_value / scale_factor);
 * bytes_per_unit = bits_per_unit / 8;
 * LOOP: i = 0, until i = bytes_per_unit
 * byte[i] = (data_unit >> 8 * i) & 0xFF);
 * write(byte);
 * **Common:**
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
	 * Size of the data unit. (Should be multiple of 8)
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
