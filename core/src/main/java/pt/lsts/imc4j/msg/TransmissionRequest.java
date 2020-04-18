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
 * Request data to be sent over a specified communication mean.
 */
public class TransmissionRequest extends Message {
	public static final int ID_STATIC = 515;

	/**
	 * The unique identifier for this request.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	/**
	 * Communication mean to be used to transfer these data.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public COMM_MEAN comm_mean = COMM_MEAN.values()[0];

	/**
	 * The name of the system where to send this message.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String destination = "";

	/**
	 * Deadline for message transmission (seconds since epoch).
	 */
	@FieldType(
			type = IMCField.TYPE_FP64
	)
	public double deadline = 0;

	/**
	 * The meaning of this field depends on the operation and is
	 * explained in the operation's description.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float range = 0f;

	/**
	 * Type of data to be transmitted.
	 * Abort and Range mode can only be used with comm_mean=ACOUSTIC
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public DATA_MODE data_mode = DATA_MODE.values()[0];

	/**
	 * Data to be transmitted if selected *data_mode* is *INLINEMSG*.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message msg_data = null;

	/**
	 * Data to be transmitted if selected *data_mode* is *TEXT*.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String txt_data = "";

	/**
	 * Data to be transmitted if selected *data_mode* is *RAW*.
	 */
	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] raw_data = new byte[0];

	public String abbrev() {
		return "TransmissionRequest";
	}

	public int mgid() {
		return 515;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			_out.writeByte((int)(comm_mean != null? comm_mean.value() : 0));
			SerializationUtils.serializePlaintext(_out, destination);
			_out.writeDouble(deadline);
			_out.writeFloat(range);
			_out.writeByte((int)(data_mode != null? data_mode.value() : 0));
			SerializationUtils.serializeInlineMsg(_out, msg_data);
			SerializationUtils.serializePlaintext(_out, txt_data);
			SerializationUtils.serializeRawdata(_out, raw_data);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			req_id = buf.getShort() & 0xFFFF;
			comm_mean = COMM_MEAN.valueOf(buf.get() & 0xFF);
			destination = SerializationUtils.deserializePlaintext(buf);
			deadline = buf.getDouble();
			range = buf.getFloat();
			data_mode = DATA_MODE.valueOf(buf.get() & 0xFF);
			msg_data = SerializationUtils.deserializeInlineMsg(buf);
			txt_data = SerializationUtils.deserializePlaintext(buf);
			raw_data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum COMM_MEAN {
		CMEAN_WIFI(0l),

		CMEAN_ACOUSTIC(1l),

		CMEAN_SATELLITE(2l),

		CMEAN_GSM(3l),

		CMEAN_ANY(4l),

		CMEAN_ALL(5l);

		protected long value;

		COMM_MEAN(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static COMM_MEAN valueOf(long value) throws IllegalArgumentException {
			for (COMM_MEAN v : COMM_MEAN.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for COMM_MEAN: "+value);
		}
	}

	public enum DATA_MODE {
		DMODE_INLINEMSG(0l),

		DMODE_TEXT(1l),

		DMODE_RAW(2l),

		DMODE_ABORT(3l),

		DMODE_RANGE(4l),

		DMODE_REVERSE_RANGE(5l);

		protected long value;

		DATA_MODE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static DATA_MODE valueOf(long value) throws IllegalArgumentException {
			for (DATA_MODE v : DATA_MODE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for DATA_MODE: "+value);
		}
	}
}
