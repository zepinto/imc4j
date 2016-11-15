package pt.lsts.imc.def;

import java.lang.IllegalArgumentException;

public enum CLoopsMask {
	NONE(0x00000000l),

	PATH(0x00000001l),

	TELEOPERATION(0x00000002l),

	ALTITUDE(0x00000004l),

	DEPTH(0x00000008l),

	ROLL(0x00000010l),

	PITCH(0x00000020l),

	YAW(0x00000040l),

	SPEED(0x00000080l),

	YAW_RATE(0x00000100l),

	VERTICAL_RATE(0x00000200l),

	TORQUE(0x00000400l),

	FORCE(0x00000800l),

	VELOCITY(0x00001000l),

	THROTTLE(0x00002000l),

	EXTERNAL(0x40000000l),

	NO_OVERRIDE(0x80000000l),

	ALL(0xFFFFFFFFl);

	protected long value;

	CLoopsMask(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static CLoopsMask valueOf(long value) throws IllegalArgumentException {
		for (CLoopsMask v : CLoopsMask.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for CLoopsMask: "+value);
	}
}
