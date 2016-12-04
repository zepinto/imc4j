package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Report of a GPS fix.
 */
public class GpsFix extends Message {
	public static final int ID_STATIC = 253;

	/**
	 * Validity of fields.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "Bitfield"
	)
	public EnumSet<VALIDITY> validity = EnumSet.noneOf(VALIDITY.class);

	/**
	 * Type of fix.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * UTC year.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int utc_year = 0;

	/**
	 * UTC month.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int utc_month = 0;

	/**
	 * UTC day.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int utc_day = 0;

	/**
	 * UTC time of the GPS fix measured in seconds since 00:00:00 (midnight).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "s"
	)
	public float utc_time = 0f;

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
	 * Height above WGS-84 ellipsoid.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float height = 0f;

	/**
	 * Number of satellites used by the GPS device to compute the
	 * solution.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int satellites = 0;

	/**
	 * Course Over Ground (true).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float cog = 0f;

	/**
	 * Speed Over Ground.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float sog = 0f;

	/**
	 * Horizontal dilution of precision.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float hdop = 0f;

	/**
	 * Vertical dilution of precision.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float vdop = 0f;

	/**
	 * Horizontal Accuracy Estimate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float hacc = 0f;

	/**
	 * Vertical Accuracy Estimate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float vacc = 0f;

	public String abbrev() {
		return "GpsFix";
	}

	public int mgid() {
		return 253;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			long _validity = 0;
			if (validity != null) {
				for (VALIDITY __validity : validity.toArray(new VALIDITY[0])) {
					_validity += __validity.value();
				}
			}
			_out.writeShort((int)_validity);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeShort(utc_year);
			_out.writeByte(utc_month);
			_out.writeByte(utc_day);
			_out.writeFloat(utc_time);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(height);
			_out.writeByte(satellites);
			_out.writeFloat(cog);
			_out.writeFloat(sog);
			_out.writeFloat(hdop);
			_out.writeFloat(vdop);
			_out.writeFloat(hacc);
			_out.writeFloat(vacc);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			long validity_val = buf.getShort() & 0xFFFF;
			validity.clear();
			for (VALIDITY VALIDITY_op : VALIDITY.values()) {
				if ((validity_val & VALIDITY_op.value()) == VALIDITY_op.value()) {
					validity.add(VALIDITY_op);
				}
			}
			type = TYPE.valueOf(buf.get() & 0xFF);
			utc_year = buf.getShort() & 0xFFFF;
			utc_month = buf.get() & 0xFF;
			utc_day = buf.get() & 0xFF;
			utc_time = buf.getFloat();
			lat = buf.getDouble();
			lon = buf.getDouble();
			height = buf.getFloat();
			satellites = buf.get() & 0xFF;
			cog = buf.getFloat();
			sog = buf.getFloat();
			hdop = buf.getFloat();
			vdop = buf.getFloat();
			hacc = buf.getFloat();
			vacc = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum VALIDITY {
		GFV_VALID_DATE(0x0001l),

		GFV_VALID_TIME(0x0002l),

		GFV_VALID_POS(0x0004l),

		GFV_VALID_COG(0x0008l),

		GFV_VALID_SOG(0x0010l),

		GFV_VALID_HACC(0x0020l),

		GFV_VALID_VACC(0x0040l),

		GFV_VALID_HDOP(0x0080l),

		GFV_VALID_VDOP(0x0100l);

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

	public enum TYPE {
		GFT_STANDALONE(0x00l),

		GFT_DIFFERENTIAL(0x01l),

		GFT_DEAD_RECKONING(0x02l),

		GFT_MANUAL_INPUT(0x03l),

		GFT_SIMULATION(0x04l);

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
