package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * Message to handle telemetry transmissions.
 */
public class TelemetryMsg extends Message {
	public static final int ID_STATIC = 190;

	/**
	 * Type of telemetry transmissions.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	/**
	 * The request identifier used to receive transmission updates.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long req_id = 0;

	/**
	 * Time, in seconds, which will be considered a non-transmitted message.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int ttl = 0;

	/**
	 * Type of telemetry transmissions.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public CODE code = CODE.values()[0];

	/**
	 * The unique identifier of this message's destination (e.g. lauv-xtreme-2, manta-0).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String destination = "";

	/**
	 * The unique identifier of this message's destination (e.g. lauv-xtreme-2, manta-0).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String Source = "";

	/**
	 * Type of telemetry transmissions.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<ACKNOWLEDGE> acknowledge = EnumSet.noneOf(ACKNOWLEDGE.class);

	/**
	 * State of the transmitted message.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATUS status = STATUS.values()[0];

	@FieldType(
			type = IMCField.TYPE_RAWDATA
	)
	public byte[] data = new byte[0];

	public String abbrev() {
		return "TelemetryMsg";
	}

	public int mgid() {
		return 190;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(type != null? type.value() : 0));
			_out.writeInt((int)req_id);
			_out.writeShort(ttl);
			_out.writeByte((int)(code != null? code.value() : 0));
			SerializationUtils.serializePlaintext(_out, destination);
			SerializationUtils.serializePlaintext(_out, Source);
			long _acknowledge = 0;
			if (acknowledge != null) {
				for (ACKNOWLEDGE __acknowledge : acknowledge.toArray(new ACKNOWLEDGE[0])) {
					_acknowledge += __acknowledge.value();
				}
			}
			_out.writeByte((int)_acknowledge);
			_out.writeByte((int)(status != null? status.value() : 0));
			SerializationUtils.serializeRawdata(_out, data);
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
			req_id = buf.getInt() & 0xFFFFFFFF;
			ttl = buf.getShort() & 0xFFFF;
			code = CODE.valueOf(buf.get() & 0xFF);
			destination = SerializationUtils.deserializePlaintext(buf);
			Source = SerializationUtils.deserializePlaintext(buf);
			long acknowledge_val = buf.get() & 0xFF;
			acknowledge.clear();
			for (ACKNOWLEDGE ACKNOWLEDGE_op : ACKNOWLEDGE.values()) {
				if ((acknowledge_val & ACKNOWLEDGE_op.value()) == ACKNOWLEDGE_op.value()) {
					acknowledge.add(ACKNOWLEDGE_op);
				}
			}
			status = STATUS.valueOf(buf.get() & 0xFF);
			data = SerializationUtils.deserializeRawdata(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		TM_TX(0x01l),

		TM_RX(0x02l),

		TM_TXSTATUS(0x03l);

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

	public enum CODE {
		TM_CODE_UNK(0x00l),

		TM_CODE_REPORT(0x01l),

		TM_CODE_IMC(0x02l),

		TM_CODE_RAW(0x03l);

		protected long value;

		CODE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static CODE valueOf(long value) throws IllegalArgumentException {
			for (CODE v : CODE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for CODE: "+value);
		}
	}

	public enum ACKNOWLEDGE {
		TM_NAK(0x00l),

		TM_AK(0x01l);

		protected long value;

		ACKNOWLEDGE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ACKNOWLEDGE valueOf(long value) throws IllegalArgumentException {
			for (ACKNOWLEDGE v : ACKNOWLEDGE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ACKNOWLEDGE: "+value);
		}
	}

	public enum STATUS {
		TM_NONE(0x00l),

		TM_DONE(1l),

		TM_FAILED(2l),

		TM_QUEUED(3l),

		TM_TRANSMIT(4l),

		TM_EXPIRED(5l),

		TM_EMPTY(6l),

		TM_INV_ADDR(7l),

		TM_INV_SIZE(8l);

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
