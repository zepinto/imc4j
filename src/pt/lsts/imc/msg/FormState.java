package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Monitoring variables to assert the formation tracking state, i.e., the mismatch between the real and the simulated aircraft position, the convergence state, etc.
 */
public class FormState extends Message {
	public static final int ID_STATIC = 510;

	/**
	 * Mismatch between the real and the simulated aircraft position.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float PosSimErr = 0f;

	/**
	 * Convergence evalution variable.
	 * Value indicates the position error to which the system is converging, tacking into account the aircraft current position error and velocity.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float Converg = 0f;

	/**
	 * Evaluation of the stream turbulence level, through the stream acceleration.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m/s/s"
	)
	public float Turbulence = 0f;

	/**
	 * Position mismatch monitoring flag.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public POSSIMMON PosSimMon = POSSIMMON.values()[0];

	/**
	 * Communications monitoring flag.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMMMON CommMon = COMMMON.values()[0];

	/**
	 * Convergence monitoring flag.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public CONVERGMON ConvergMon = CONVERGMON.values()[0];

	public String abbrev() {
		return "FormState";
	}

	public int mgid() {
		return 510;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(PosSimErr);
			_out.writeFloat(Converg);
			_out.writeFloat(Turbulence);
			_out.writeByte((int)(PosSimMon != null? PosSimMon.value() : 0));
			_out.writeByte((int)(CommMon != null? CommMon.value() : 0));
			_out.writeByte((int)(ConvergMon != null? ConvergMon.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			PosSimErr = buf.getFloat();
			Converg = buf.getFloat();
			Turbulence = buf.getFloat();
			PosSimMon = POSSIMMON.valueOf(buf.get() & 0xFF);
			CommMon = COMMMON.valueOf(buf.get() & 0xFF);
			ConvergMon = CONVERGMON.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum POSSIMMON {
		POS_OK(0l),

		POS_WRN(1l),

		POS_LIM(2l);

		protected long value;

		POSSIMMON(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static POSSIMMON valueOf(long value) throws IllegalArgumentException {
			for (POSSIMMON v : POSSIMMON.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for POSSIMMON: "+value);
		}
	}

	public enum COMMMON {
		COMMS_OK(0l),

		COMMS_TIMEOUT(1l);

		protected long value;

		COMMMON(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static COMMMON valueOf(long value) throws IllegalArgumentException {
			for (COMMMON v : COMMMON.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for COMMMON: "+value);
		}
	}

	public enum CONVERGMON {
		CONV_OK(0l),

		CONV_TIMEOUT(1l);

		protected long value;

		CONVERGMON(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static CONVERGMON valueOf(long value) throws IllegalArgumentException {
			for (CONVERGMON v : CONVERGMON.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for CONVERGMON: "+value);
		}
	}
}
