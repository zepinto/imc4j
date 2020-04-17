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

public class SoiState extends Message {
	public static final int ID_STATIC = 853;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public STATE state = STATE.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int plan_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int wpt_id = 0;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int settings_chk = 0;

	public String abbrev() {
		return "SoiState";
	}

	public int mgid() {
		return 853;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte((int)(state != null? state.value() : 0));
			_out.writeShort(plan_id);
			_out.writeByte(wpt_id);
			_out.writeShort(settings_chk);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			state = STATE.valueOf(buf.get() & 0xFF);
			plan_id = buf.getShort() & 0xFFFF;
			wpt_id = buf.get() & 0xFF;
			settings_chk = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum STATE {
		SOISTATE_EXEC(1l),

		SOISTATE_IDLE(2l),

		SOISTATE_INACTIVE(3l);

		protected long value;

		STATE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static STATE valueOf(long value) throws IllegalArgumentException {
			for (STATE v : STATE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for STATE: "+value);
		}
	}
}
