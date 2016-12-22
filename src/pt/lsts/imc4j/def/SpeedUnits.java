package pt.lsts.imc4j.def;

import java.lang.IllegalArgumentException;

public enum SpeedUnits {
	METERS_PS(0l),

	RPM(1l),

	PERCENTAGE(2l);

	protected long value;

	SpeedUnits(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static SpeedUnits valueOf(long value) throws IllegalArgumentException {
		for (SpeedUnits v : SpeedUnits.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for SpeedUnits: "+value);
	}
}
