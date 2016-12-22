package pt.lsts.imc4j.def;

import java.lang.IllegalArgumentException;

public enum ZUnits {
	NONE(0l),

	DEPTH(1l),

	ALTITUDE(2l),

	HEIGHT(3l);

	protected long value;

	ZUnits(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static ZUnits valueOf(long value) throws IllegalArgumentException {
		for (ZUnits v : ZUnits.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for ZUnits: "+value);
	}
}
