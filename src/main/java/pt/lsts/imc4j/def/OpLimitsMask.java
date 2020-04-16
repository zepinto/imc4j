package pt.lsts.imc4j.def;

import java.lang.IllegalArgumentException;

public enum OpLimitsMask {
	MAX_DEPTH(0x01l),

	MIN_ALT(0x02l),

	MAX_ALT(0x04l),

	MIN_SPEED(0x08l),

	MAX_SPEED(0x10l),

	MAX_VRATE(0x20l),

	AREA(0x40l);

	protected long value;

	OpLimitsMask(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static OpLimitsMask valueOf(long value) throws IllegalArgumentException {
		for (OpLimitsMask v : OpLimitsMask.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for OpLimitsMask: "+value);
	}
}
