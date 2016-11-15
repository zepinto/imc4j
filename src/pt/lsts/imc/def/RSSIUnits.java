package pt.lsts.imc.def;

import java.lang.IllegalArgumentException;

public enum RSSIUnits {
	dB(0l),

	PERCENTAGE(1l);

	protected long value;

	RSSIUnits(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static RSSIUnits valueOf(long value) throws IllegalArgumentException {
		for (RSSIUnits v : RSSIUnits.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for RSSIUnits: "+value);
	}
}
