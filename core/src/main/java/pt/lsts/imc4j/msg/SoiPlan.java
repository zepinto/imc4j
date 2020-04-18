package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

public class SoiPlan extends Message {
	public static final int ID_STATIC = 851;

	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int plan_id = 0;

	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<SoiWaypoint> waypoints = new ArrayList<>();

	public String abbrev() {
		return "SoiPlan";
	}

	public int mgid() {
		return 851;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(plan_id);
			SerializationUtils.serializeMsgList(_out, waypoints);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			plan_id = buf.getShort() & 0xFFFF;
			waypoints = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
