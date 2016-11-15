package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.SpeedUnits;
import pt.lsts.imc.util.SerializationUtils;

/**
 * This maneuver triggers an external controller that will guide the vehicle during a specified duration
 * of time or until it relinquishes control using (ManeuverDone). The external controller is allowed to
 * drive the vehicle only inside the specified boundaries.
 */
public class AutonomousSection extends Maneuver {
	public static final int ID_STATIC = 493;

	/**
	 * WGS-84 Latitude of the initial location.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude of the initial location.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Maneuver speed reference.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float speed = 0f;

	/**
	 * Speed units.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SpeedUnits speed_units = SpeedUnits.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<LIMITS> limits = EnumSet.noneOf(LIMITS.class);

	/**
	 * Maximum depth the autonomous controller is allowed to drive to.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m"
	)
	public double max_depth = 0;

	/**
	 * Minimum altitude the autonomous controller is allowed to drive to.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m"
	)
	public double min_alt = 0;

	/**
	 * The time after which this maneuver should be stopped (if still active and TIMEOUT is enforced).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double time_limit = 0;

	/**
	 * The boundaries of the admissable area for this autonomous section.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<PolygonVertex> area_limits = new ArrayList<>();

	/**
	 * The name of the controlling agent that will be allowed to guide the vehicle during the AutononousSection.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String controller = "";

	/**
	 * Custom settings for maneuver.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public String custom = "";

	public int mgid() {
		return 493;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(speed);
			_out.writeByte((int)speed_units.value());
			long _limits = 0;
			for (LIMITS __limits : limits.toArray(new LIMITS[0])) {
				_limits += __limits.value();
			}
			_out.writeByte((int)_limits);
			_out.writeDouble(max_depth);
			_out.writeDouble(min_alt);
			_out.writeDouble(time_limit);
			SerializationUtils.serializeMsgList(_out, area_limits);
			SerializationUtils.serializePlaintext(_out, controller);
			SerializationUtils.serializePlaintext(_out, custom);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			lat = buf.getDouble();
			lon = buf.getDouble();
			speed = buf.getFloat();
			speed_units = SpeedUnits.valueOf(buf.get() & 0xFF);
			long limits_val = buf.get() & 0xFF;
			limits.clear();
			for (LIMITS LIMITS_op : LIMITS.values()) {
				if ((limits_val & LIMITS_op.value()) == LIMITS_op.value()) {
					limits.add(LIMITS_op);
				}
			}
			max_depth = buf.getDouble();
			min_alt = buf.getDouble();
			time_limit = buf.getDouble();
			area_limits = SerializationUtils.deserializeMsgList(buf);
			controller = SerializationUtils.deserializePlaintext(buf);
			custom = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum LIMITS {
		ENFORCE_DEPTH(0x01l),

		ENFORCE_ALTITUDE(0x02l),

		ENFORCE_TIMEOUT(0x04l),

		ENFORCE_AREA2D(0x08l);

		protected long value;

		LIMITS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static LIMITS valueOf(long value) throws IllegalArgumentException {
			for (LIMITS v : LIMITS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for LIMITS: "+value);
		}
	}
}
