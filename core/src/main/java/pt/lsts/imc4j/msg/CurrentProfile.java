package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Contains a profile of water velocities measured relative to the vehicle
 * velocity, represented in the specified coordinate system.
 */
public class CurrentProfile extends Message {
	public static final int ID_STATIC = 1014;

	/**
	 * Number of ADCP beams.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int nbeams = 0;

	/**
	 * Number of ADCP cells.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int ncells = 0;

	/**
	 * Coordinate system of the velocity measurement.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<COORD_SYS> coord_sys = EnumSet.noneOf(COORD_SYS.class);

	/**
	 * List of current profile measurement cells.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<CurrentProfileCell> profile = new ArrayList<>();

	public String abbrev() {
		return "CurrentProfile";
	}

	public int mgid() {
		return 1014;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(nbeams);
			_out.writeByte(ncells);
			long _coord_sys = 0;
			if (coord_sys != null) {
				for (COORD_SYS __coord_sys : coord_sys.toArray(new COORD_SYS[0])) {
					_coord_sys += __coord_sys.value();
				}
			}
			_out.writeByte((int)_coord_sys);
			SerializationUtils.serializeMsgList(_out, profile);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			nbeams = buf.get() & 0xFF;
			ncells = buf.get() & 0xFF;
			long coord_sys_val = buf.get() & 0xFF;
			coord_sys.clear();
			for (COORD_SYS COORD_SYS_op : COORD_SYS.values()) {
				if ((coord_sys_val & COORD_SYS_op.value()) == COORD_SYS_op.value()) {
					coord_sys.add(COORD_SYS_op);
				}
			}
			profile = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum COORD_SYS {
		UTF_XYZ(0x01l),

		UTF_NED(0x02l),

		UTF_BEAMS(0x04l);

		protected long value;

		COORD_SYS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static COORD_SYS valueOf(long value) throws IllegalArgumentException {
			for (COORD_SYS v : COORD_SYS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for COORD_SYS: "+value);
		}
	}
}
