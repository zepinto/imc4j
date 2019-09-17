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
 * Presence of Communication Interfaces query.
 */
public class CommSystemsQuery extends Message {
	public static final int ID_STATIC = 189;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<TYPE> type = EnumSet.noneOf(TYPE.class);

	/**
	 * Communication interface to be used for reports.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "Bitfield"
	)
	public EnumSet<COMM_INTERFACE> comm_interface = EnumSet.noneOf(COMM_INTERFACE.class);

	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "Enumerated"
	)
	public MODEL model = MODEL.values()[0];

	/**
	 * Comma separated list of known Radio system names.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "List"
	)
	public String list = "";

	public String abbrev() {
		return "CommSystemsQuery";
	}

	public int mgid() {
		return 189;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			long _type = 0;
			if (type != null) {
				for (TYPE __type : type.toArray(new TYPE[0])) {
					_type += __type.value();
				}
			}
			_out.writeByte((int)_type);
			long _comm_interface = 0;
			if (comm_interface != null) {
				for (COMM_INTERFACE __comm_interface : comm_interface.toArray(new COMM_INTERFACE[0])) {
					_comm_interface += __comm_interface.value();
				}
			}
			_out.writeShort((int)_comm_interface);
			_out.writeShort((int)(model != null? model.value() : 0));
			SerializationUtils.serializePlaintext(_out, list);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			long type_val = buf.get() & 0xFF;
			type.clear();
			for (TYPE TYPE_op : TYPE.values()) {
				if ((type_val & TYPE_op.value()) == TYPE_op.value()) {
					type.add(TYPE_op);
				}
			}
			long comm_interface_val = buf.getShort() & 0xFFFF;
			comm_interface.clear();
			for (COMM_INTERFACE COMM_INTERFACE_op : COMM_INTERFACE.values()) {
				if ((comm_interface_val & COMM_INTERFACE_op.value()) == COMM_INTERFACE_op.value()) {
					comm_interface.add(COMM_INTERFACE_op);
				}
			}
			model = MODEL.valueOf(buf.getShort() & 0xFFFF);
			list = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		CIQ_QUERY(0x01l),

		CIQ_REPLY(0x02l);

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

	public enum COMM_INTERFACE {
		CIQ_ACOUSTIC(0x01l),

		CIQ_SATELLITE(0x02l),

		CIQ_GSM(0x04l),

		CIQ_MOBILE(0x08l),

		CIQ_RADIO(0x10l);

		protected long value;

		COMM_INTERFACE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static COMM_INTERFACE valueOf(long value) throws IllegalArgumentException {
			for (COMM_INTERFACE v : COMM_INTERFACE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for COMM_INTERFACE: "+value);
		}
	}

	public enum MODEL {
		CIQ_UNKNOWN(0x00l),

		CIQ_M3DR(0x01l),

		CIQ_RDFXXXXPTP(0x02l);

		protected long value;

		MODEL(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static MODEL valueOf(long value) throws IllegalArgumentException {
			for (MODEL v : MODEL.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for MODEL: "+value);
		}
	}
}
