package pt.lsts.imc4j.def;

import java.lang.IllegalArgumentException;

public enum Boolean {
	FALSE(0l),

	TRUE(1l);

	protected long value;

	Boolean(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static Boolean valueOf(long value) throws IllegalArgumentException {
		for (Boolean v : Boolean.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for Boolean: "+value);
	}
}
