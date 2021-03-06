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
import pt.lsts.imc4j.util.TupleList;

/**
 * This message is used to order the generation of plans based on
 * id and set of parameters.
 */
public class PlanGeneration extends Message {
	public static final int ID_STATIC = 562;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public CMD cmd = CMD.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * The name of the plan to be generated.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	/**
	 * An optional list of parameters to be used by the plan
	 * generation module.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList params = new TupleList("");

	public String abbrev() {
		return "PlanGeneration";
	}

	public int mgid() {
		return 562;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(cmd != null? cmd.value() : 0));
			_out.writeByte((int)(op != null? op.value() : 0));
			SerializationUtils.serializePlaintext(_out, plan_id);
			SerializationUtils.serializePlaintext(_out, params == null? null : params.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			cmd = CMD.valueOf(buf.get() & 0xFF);
			op = OP.valueOf(buf.get() & 0xFF);
			plan_id = SerializationUtils.deserializePlaintext(buf);
			params = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum CMD {
		CMD_GENERATE(0l),

		CMD_EXECUTE(1l);

		protected long value;

		CMD(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static CMD valueOf(long value) throws IllegalArgumentException {
			for (CMD v : CMD.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for CMD: "+value);
		}
	}

	public enum OP {
		OP_REQUEST(0l),

		OP_ERROR(1l),

		OP_SUCCESS(2l);

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
