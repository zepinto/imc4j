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

public class IridiumTxStatus extends Message {
	public static final int ID_STATIC = 172;

	/**
	 * The request identifier used to receive transmission updates
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int req_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATUS status = STATUS.values()[0];

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String text = "";

	public String abbrev() {
		return "IridiumTxStatus";
	}

	public int mgid() {
		return 172;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(req_id);
			_out.writeByte((int)(status != null? status.value() : 0));
			SerializationUtils.serializePlaintext(_out, text);
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
			status = STATUS.valueOf(buf.get() & 0xFF);
			text = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATUS {
		TXSTATUS_OK(1l),

		TXSTATUS_ERROR(2l),

		TXSTATUS_QUEUED(3l),

		TXSTATUS_TRANSMIT(4l),

		TXSTATUS_EXPIRED(5l),

		TXSTATUS_EMPTY(6l);

		protected long value;

		STATUS(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATUS valueOf(long value) throws IllegalArgumentException {
			for (STATUS v : STATUS.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATUS: "+value);
		}
	}
}
