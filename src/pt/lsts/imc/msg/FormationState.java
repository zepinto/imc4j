package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Monitoring variables to assert the formation tracking state, i.e., the mismatch between the real and the simulated aircraft position, the convergence state, etc.
 */
public class FormationState extends Message {
	public static final int ID_STATIC = 512;

	/**
	 * Indicates if the message is a request, or a reply to a previous request.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Operation to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

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

	public int mgid() {
		return 512;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)type.value());
			_out.writeByte((int)op.value());
			_out.writeFloat(PosSimErr);
			_out.writeFloat(Converg);
			_out.writeFloat(Turbulence);
			_out.writeByte((int)PosSimMon.value());
			_out.writeByte((int)CommMon.value());
			_out.writeByte((int)ConvergMon.value());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			type = TYPE.valueOf(buf.get() & 0xFF);
			op = OP.valueOf(buf.get() & 0xFF);
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

	public enum TYPE {
		FC_REQUEST(0l),

		FC_REPORT(1l);

		protected long value;

		TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static TYPE valueOf(long value) throws IllegalArgumentException {
			for (TYPE v : TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for TYPE: "+value);
		}
	}

	public enum OP {
		OP_START(0l),

		OP_STOP(1l);

		protected long value;

		OP(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static OP valueOf(long value) throws IllegalArgumentException {
			for (OP v : OP.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for OP: "+value);
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
