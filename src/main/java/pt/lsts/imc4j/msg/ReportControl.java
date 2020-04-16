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
 * This message is sent to trigger reports to a destination system.
 */
public class ReportControl extends Message {
	public static final int ID_STATIC = 513;

	/**
	 * Operation to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public OP op = OP.values()[0];

	/**
	 * Communication interface to be used for reports.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<COMM_INTERFACE> comm_interface = EnumSet.noneOf(COMM_INTERFACE.class);

	/**
	 * Desired periodicity for scheduled reports.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int period = 0;

	/**
	 * Destination Address to be filled where applicable. It should be
	 * interpreted differently depending on communication interface.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_dst = "";

	public String abbrev() {
		return "ReportControl";
	}

	public int mgid() {
		return 513;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(op != null? op.value() : 0));
			long _comm_interface = 0;
			if (comm_interface != null) {
				for (COMM_INTERFACE __comm_interface : comm_interface.toArray(new COMM_INTERFACE[0])) {
					_comm_interface += __comm_interface.value();
				}
			}
			_out.writeByte((int)_comm_interface);
			_out.writeShort(period);
			SerializationUtils.serializePlaintext(_out, sys_dst);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			op = OP.valueOf(buf.get() & 0xFF);
			long comm_interface_val = buf.get() & 0xFF;
			comm_interface.clear();
			for (COMM_INTERFACE COMM_INTERFACE_op : COMM_INTERFACE.values()) {
				if ((comm_interface_val & COMM_INTERFACE_op.value()) == COMM_INTERFACE_op.value()) {
					comm_interface.add(COMM_INTERFACE_op);
				}
			}
			period = buf.getShort() & 0xFFFF;
			sys_dst = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum OP {
		OP_REQUEST_START(0l),

		OP_STARTED(1l),

		OP_REQUEST_STOP(2l),

		OP_STOPPED(3l),

		OP_REQUEST_REPORT(4l),

		OP_REPORT_SENT(5l);

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

	public enum COMM_INTERFACE {
		CI_ACOUSTIC(0x01l),

		CI_SATELLITE(0x02l),

		CI_GSM(0x04l),

		CI_MOBILE(0x08l),

		CI_RADIO(0x10l);

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
}
