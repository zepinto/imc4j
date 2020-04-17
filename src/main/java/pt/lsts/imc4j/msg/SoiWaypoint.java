package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

public class SoiWaypoint extends Message {
	public static final int ID_STATIC = 850;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "°"
	)
	public float lat = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "°"
	)
	public float lon = 0f;

	@FieldType(
			type = IMCField.TYPE_UINT32
	)
	public long eta = 0;

	@FieldType(
			type = IMCField.TYPE_UINT16,
			units = "s"
	)
	public int duration = 0;

	public String abbrev() {
		return "SoiWaypoint";
	}

	public int mgid() {
		return 850;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(lat);
			_out.writeFloat(lon);
			_out.writeInt((int)eta);
			_out.writeShort(duration);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			lat = buf.getFloat();
			lon = buf.getFloat();
			eta = buf.getInt() & 0xFFFFFFFF;
			duration = buf.getShort() & 0xFFFF;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
