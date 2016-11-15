package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.CLoopsMask;

/**
 * Enable or disable control loops.
 */
public class ControlLoops extends Message {
	public static final int ID_STATIC = 507;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ENABLE enable = ENABLE.values()[0];

	/**
	 * Control loop mask.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32,
			units = "Bitfield"
	)
	public EnumSet<CLoopsMask> mask = EnumSet.noneOf(CLoopsMask.class);

	/**
	 * Unsigned integer reference for the scope of the control loop message.
	 * Scope reference should only be set by a maneuver.
	 * Should be set to an always increasing reference at the time of dispatching this message.
	 * Lower level controllers must inherit the same scope reference sent by maneuver.
	 * This same scope reference must be sent down to lower control layers.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long scope_ref = 0;

	public int mgid() {
		return 507;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(enable != null? enable.value() : 0));
			long _mask = 0;
			if (mask != null) {
				for (CLoopsMask __mask : mask.toArray(new CLoopsMask[0])) {
					_mask += __mask.value();
				}
			}
			_out.writeInt((int)(int)_mask);
			_out.writeInt((int)scope_ref);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			enable = ENABLE.valueOf(buf.get() & 0xFF);
			long mask_val = buf.getInt() & 0xFFFFFFFF;
			mask.clear();
			for (CLoopsMask CLoopsMask_op : CLoopsMask.values()) {
				if ((mask_val & CLoopsMask_op.value()) == CLoopsMask_op.value()) {
					mask.add(CLoopsMask_op);
				}
			}
			scope_ref = buf.getInt() & 0xFFFFFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ENABLE {
		CL_DISABLE(0l),

		CL_ENABLE(1l);

		protected long value;

		ENABLE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ENABLE valueOf(long value) throws IllegalArgumentException {
			for (ENABLE v : ENABLE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ENABLE: "+value);
		}
	}
}
