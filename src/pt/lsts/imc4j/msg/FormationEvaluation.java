package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Formation control performance evaluation variables.
 */
public class FormationEvaluation extends Message {
	public static final int ID_STATIC = 823;

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
	 * Mean position error relative to the formation reference.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float err_mean = 0f;

	/**
	 * Overall minimum distance to any other vehicle in the formation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float dist_min_abs = 0f;

	/**
	 * Mean minimum distance to any other vehicle in the formation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float dist_min_mean = 0f;

	/**
	 * Mean minimum distance to any other vehicle in the formation.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float roll_rate_mean = 0f;

	/**
	 * Period over which the evaluation data is averaged.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float time = 0f;

	/**
	 * Formation controller paramenters during the evaluation period.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public FormationControlParams ControlParams = null;

	public String abbrev() {
		return "FormationEvaluation";
	}

	public int mgid() {
		return 823;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeByte((int)(op != null? op.value() : 0));
			_out.writeFloat(err_mean);
			_out.writeFloat(dist_min_abs);
			_out.writeFloat(dist_min_mean);
			_out.writeFloat(roll_rate_mean);
			_out.writeFloat(time);
			SerializationUtils.serializeInlineMsg(_out, ControlParams);
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
			err_mean = buf.getFloat();
			dist_min_abs = buf.getFloat();
			dist_min_mean = buf.getFloat();
			roll_rate_mean = buf.getFloat();
			time = buf.getFloat();
			ControlParams = SerializationUtils.deserializeInlineMsg(buf);
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

		OP_STOP(1l),

		OP_READY(2l),

		OP_EXECUTING(3l),

		OP_FAILURE(4l);

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
}
