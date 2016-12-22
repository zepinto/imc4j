package pt.lsts.imc4j.def;

import java.lang.IllegalArgumentException;

public enum ControlledMode {
	RELINQUISH_HANDOFF_CTL(0l),

	REQUEST_CTL(1l),

	OVERRIDE_CTL(2l);

	protected long value;

	ControlledMode(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static ControlledMode valueOf(long value) throws IllegalArgumentException {
		for (ControlledMode v : ControlledMode.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for ControlledMode: "+value);
	}
}
