package pt.lsts.imc.def;

import java.lang.IllegalArgumentException;

public enum SystemType {
	CCU(0l),

	HUMANSENSOR(1l),

	UUV(2l),

	USV(3l),

	UAV(4l),

	UGV(5l),

	STATICSENSOR(6l),

	MOBILESENSOR(7l),

	WSN(8l);

	protected long value;

	SystemType(long value) {
		this.value = value;
	}

	public long value() {
		return value;
	}

	public static SystemType valueOf(long value) throws IllegalArgumentException {
		for (SystemType v : SystemType.values()) {
			if (v.value == value) {
				return v;
			}
		}
		throw new IllegalArgumentException("Invalid value for SystemType: "+value);
	}
}
