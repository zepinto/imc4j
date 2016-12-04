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
 * Report of an RTK-GPS fix.
 */
public class GpsFixRtk extends Message {
	public static final int ID_STATIC = 293;

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
	 * GPS Time of Week.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long tow = 0;

	/**
	 * WGS-84 Latitude coordinate of the base.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double base_lat = 0;

	/**
	 * WGS-84 Longitude coordinate of the base.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double base_lon = 0;

	/**
	 * Height above WGS-84 ellipsoid of the base.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float base_height = 0f;

	/**
	 * Baseline North coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float n = 0f;

	/**
	 * Baseline East coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float e = 0f;

	/**
	 * Baseline Down coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float d = 0f;

	/**
	 * Velocity North coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float v_n = 0f;

	/**
	 * Velocity East coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float v_e = 0f;

	/**
	 * Velocity Down coordinate.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s"
	)
	public float v_d = 0f;

	/**
	 * Number of satellites used in solution.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int satellites = 0;

	/**
	 * Number of hypotheses in the Integer Ambiguity Resolution (smaller is better).
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int iar_hyp = 0;

	/**
	 * Quality ratio of Integer Ambiguity Resolution (bigger is better).
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float iar_ratio = 0f;

	public String abbrev() {
		return "GpsFixRtk";
	}

	public int mgid() {
		return 293;
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
			_out.writeInt((int)tow);
			_out.writeDouble(base_lat);
			_out.writeDouble(base_lon);
			_out.writeFloat(base_height);
			_out.writeFloat(n);
			_out.writeFloat(e);
			_out.writeFloat(d);
			_out.writeFloat(v_n);
			_out.writeFloat(v_e);
			_out.writeFloat(v_d);
			_out.writeByte(satellites);
			_out.writeShort(iar_hyp);
			_out.writeFloat(iar_ratio);
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
			tow = buf.getInt() & 0xFFFFFFFF;
			base_lat = buf.getDouble();
			base_lon = buf.getDouble();
			base_height = buf.getFloat();
			n = buf.getFloat();
			e = buf.getFloat();
			d = buf.getFloat();
			v_n = buf.getFloat();
			v_e = buf.getFloat();
			v_d = buf.getFloat();
			satellites = buf.get() & 0xFF;
			iar_hyp = buf.getShort() & 0xFFFF;
			iar_ratio = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum VALIDITY {
		RFV_VALID_TIME(0x0001l),

		RFV_VALID_BASE(0x0002l),

		RFV_VALID_POS(0x0004l),

		RFV_VALID_VEL(0x0008l);

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
		RTK_NONE(0x00l),

		RTK_OBS(0x01l),

		RTK_FLOAT(0x02l),

		RTK_FIXED(0x03l);

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
