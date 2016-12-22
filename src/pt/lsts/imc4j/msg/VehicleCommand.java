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
 * Vehicle command.
 */
public class VehicleCommand extends Message {
	public static final int ID_STATIC = 501;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * Request ID
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int request_id = 0;

	/**
	 * The type of command/action to be performed
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMMAND command = COMMAND.values()[0];

	/**
	 * Maneuver to be executed (for 'EXEC_MANEUVER' command)
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Maneuver maneuver = null;

	/**
	 * Amount of time to calibrate
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int calib_time = 0;

	/**
	 * Complementary human-readable information for replies.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String info = "";

	public String abbrev() {
		return "VehicleCommand";
	}

	public int mgid() {
		return 501;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeShort(request_id);
			_out.writeByte((int)(command != null? command.value() : 0));
			SerializationUtils.serializeInlineMsg(_out, maneuver);
			_out.writeShort(calib_time);
			SerializationUtils.serializePlaintext(_out, info);
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
			request_id = buf.getShort() & 0xFFFF;
			command = COMMAND.valueOf(buf.get() & 0xFF);
			maneuver = SerializationUtils.deserializeInlineMsg(buf);
			calib_time = buf.getShort() & 0xFFFF;
			info = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		VC_REQUEST(0l),

		VC_SUCCESS(1l),

		VC_IN_PROGRESS(2l),

		VC_FAILURE(3l);

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

	public enum COMMAND {
		VC_EXEC_MANEUVER(0l),

		VC_STOP_MANEUVER(1l),

		VC_START_CALIBRATION(2l),

		VC_STOP_CALIBRATION(3l);

		protected long value;

		COMMAND(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static COMMAND valueOf(long value) throws IllegalArgumentException {
			for (COMMAND v : COMMAND.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for COMMAND: "+value);
		}
	}
}
