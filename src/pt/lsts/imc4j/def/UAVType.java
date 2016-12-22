package pt.lsts.imc4j.def;

import java.lang.IllegalArgumentException;

public enum UAVType {
	FIXEDWING(0l),

	COPTER(1l),

	VTOL(2l);

	protected long value;

	UAVType(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static UAVType valueOf(long value) throws IllegalArgumentException {
		for (UAVType v : UAVType.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for UAVType: "+value);
	}
}
