package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

public class FollowRefState extends Message {
	public static final int ID_STATIC = 480;

	/**
	 * The IMC identifier of the source system that is allowed to control the vehicle.
	 * If the value ''0xFFFF'' is used, any system is allowed to command references.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int control_src = 0;

	/**
	 * The entity identifier of the entity that is allowed to control the vehicle.
	 * If the value ''0xFF'' is used, any entity is allowed to command references.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int control_ent = 0;

	/**
	 * Reference currently being followed.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Reference reference = null;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<PROXIMITY> proximity = EnumSet.noneOf(PROXIMITY.class);

	public String abbrev() {
		return "FollowRefState";
	}

	public int mgid() {
		return 480;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(control_src);
			_out.writeByte(control_ent);
			SerializationUtils.serializeInlineMsg(_out, reference);
			_out.writeByte((int)(state != null? state.value() : 0));
			long _proximity = 0;
			if (proximity != null) {
				for (PROXIMITY __proximity : proximity.toArray(new PROXIMITY[0])) {
					_proximity += __proximity.value();
				}
			}
			_out.writeByte((int)_proximity);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			control_src = buf.getShort() & 0xFFFF;
			control_ent = buf.get() & 0xFF;
			reference = SerializationUtils.deserializeInlineMsg(buf);
			state = STATE.valueOf(buf.get() & 0xFF);
			long proximity_val = buf.get() & 0xFF;
			proximity.clear();
			for (PROXIMITY PROXIMITY_op : PROXIMITY.values()) {
				if ((proximity_val & PROXIMITY_op.value()) == PROXIMITY_op.value()) {
					proximity.add(PROXIMITY_op);
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		FR_WAIT(1l),

		FR_GOTO(2l),

		FR_LOITER(3l),

		FR_HOVER(4l),

		FR_ELEVATOR(5l),

		FR_TIMEOUT(6l);

		protected long value;

		STATE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATE valueOf(long value) throws IllegalArgumentException {
			for (STATE v : STATE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATE: "+value);
		}
	}

	public enum PROXIMITY {
		PROX_FAR(0x01l),

		PROX_XY_NEAR(0x02l),

		PROX_Z_NEAR(0x04l);

		protected long value;

		PROXIMITY(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static PROXIMITY valueOf(long value) throws IllegalArgumentException {
			for (PROXIMITY v : PROXIMITY.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for PROXIMITY: "+value);
		}
	}
}
