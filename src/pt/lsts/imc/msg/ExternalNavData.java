package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * This message is a representation of the state of the vehicle,
 * as seen by an external navigation computer.
 * An example usage is when DUNE is used with ardupilot. The
 * data gathered from the autopilot is a complete navigation
 * solution.
 * ExternalNavData contains an inline Estimated State, which
 * is a complete description of the system
 * in terms of parameters such as position, orientation and
 * velocities at a particular moment in time.
 * The Type field selects wether the navigation data is a
 * full state estimation, or only concerns attitude or
 * position/velocity.
 */
public class ExternalNavData extends Message {
	public static final int ID_STATIC = 294;

	/**
	 * External Navigation Data.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public EstimatedState state = null;

	/**
	 * The type of external navigation data
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	public int mgid() {
		return 294;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializeInlineMsg(_out, state);
			_out.writeByte((int)type.value());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			state = SerializationUtils.deserializeInlineMsg(buf);
			type = TYPE.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		EXTNAV_FULL(0l),

		EXTNAV_AHRS(1l),

		EXTNAV_POSREF(2l);

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
