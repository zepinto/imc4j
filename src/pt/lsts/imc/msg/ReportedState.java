package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * A vehicle state that is reported to other consoles (including PDAConsole). Source can be acoustic tracker, SMS, Wi-Fi, etc...
 */
public class ReportedState extends Message {
	public static final int ID_STATIC = 600;

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

	/**
	 * The reported depth. In the case of not knowing the depth 0 will be reported.
	 * Airplanes usually have negative values (por positive altitude).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "m"
	)
	public double depth = 0;

	/**
	 * The phi Euler angle from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double roll = 0;

	/**
	 * The theta Euler angle from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double pitch = 0;

	/**
	 * The psi Euler angle from the vehicle's attitude.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double yaw = 0;

	/**
	 * The time when the packet was sent, as seen by the packet
	 * dispatcher. The number of seconds is represented in Universal
	 * Coordinated Time (UCT) in seconds since Jan 1, 1970 using IEEE
	 * double precision floating point numbers.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double rcp_time = 0;

	/**
	 * The id of the system whose position is being reported (it can be a vehicle's id, a boat name, etc)
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sid = "";

	/**
	 * How the position was received/calculated
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public S_TYPE s_type = S_TYPE.values()[0];

	public int mgid() {
		return 600;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeDouble(depth);
			_out.writeDouble(roll);
			_out.writeDouble(pitch);
			_out.writeDouble(yaw);
			_out.writeDouble(rcp_time);
			SerializationUtils.serializePlaintext(_out, sid);
			_out.writeByte((int)s_type.value());
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
			depth = buf.getDouble();
			roll = buf.getDouble();
			pitch = buf.getDouble();
			yaw = buf.getDouble();
			rcp_time = buf.getDouble();
			sid = SerializationUtils.deserializePlaintext(buf);
			s_type = S_TYPE.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum S_TYPE {
		STYPE_WI_FI(0l),

		STYPE_TRACKER(1l),

		STYPE_SMS(2l),

		STYPE_ACOUSTIC_MODEM(3l),

		STYPE_UNKNOWN(254l);

		protected long value;

		S_TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static S_TYPE valueOf(long value) throws IllegalArgumentException {
			for (S_TYPE v : S_TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for S_TYPE: "+value);
		}
	}
}
