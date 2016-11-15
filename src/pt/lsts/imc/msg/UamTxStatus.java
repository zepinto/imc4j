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
import pt.lsts.imc.util.SerializationUtils;

public class UamTxStatus extends Message {
	public static final int ID_STATIC = 816;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int seq = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public VALUE value = VALUE.values()[0];

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String error = "";

	public int mgid() {
		return 816;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(seq);
			_out.writeByte((int)(value != null? value.value() : 0));
			SerializationUtils.serializePlaintext(_out, error);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			seq = buf.getShort() & 0xFFFF;
			value = VALUE.valueOf(buf.get() & 0xFF);
			error = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum VALUE {
		UTS_DONE(0l),

		UTS_FAILED(1l),

		UTS_CANCELED(2l),

		UTS_BUSY(3l),

		UTS_INV_ADDR(4l),

		UTS_IP(5l);

		protected long value;

		VALUE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static VALUE valueOf(long value) throws IllegalArgumentException {
			for (VALUE v : VALUE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for VALUE: "+value);
		}
	}
}
