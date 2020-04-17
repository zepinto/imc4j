package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;

public class HistoricSample extends RemoteData {
	public static final int ID_STATIC = 186;

	/**
	 * The IMC identifier of the system that produced this sample.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int sys_id = 0;

	/**
	 * The priority for this data sample. Default priority is 0. Samples with
	 * higher priorities will *always* be transmitted before samples with lower
	 * priorities. Samples with -127 priority will not be transmitted but just
	 * logged to disk locally.
	 */
	@FieldType(
			type = IMCField.TYPE_INT8
	)
	public int priority = 0;

	/**
	 * Northing offsets relative to base latitude / longitude expressed in the enclosing `HistoricData` message.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "m"
	)
	public int x = 0;

	/**
	 * Easting offsets relative to base latitude / longitude expressed in the enclosing `HistoricData` message.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "m"
	)
	public int y = 0;

	/**
	 * Altitude / depth offsets relative to sea level expressed in decimeters.
	 * Negative values mean depth and positive values mean altitude.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "dm"
	)
	public int z = 0;

	/**
	 * Time offset in seconds relative to the base time expressed in the enclosing `HistoricData` message.
	 */
	@FieldType(
			type = IMCField.TYPE_INT16,
			units = "s"
	)
	public int t = 0;

	@FieldType(
			type = IMCField.TYPE_MESSAGE
	)
	public Message sample = null;

	public String abbrev() {
		return "HistoricSample";
	}

	public int mgid() {
		return 186;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(sys_id);
			_out.writeByte(priority);
			_out.writeShort(x);
			_out.writeShort(y);
			_out.writeShort(z);
			_out.writeShort(t);
			SerializationUtils.serializeInlineMsg(_out, sample);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			sys_id = buf.getShort() & 0xFFFF;
			priority = buf.get();
			x = buf.getShort();
			y = buf.getShort();
			z = buf.getShort();
			t = buf.getShort();
			sample = SerializationUtils.deserializeInlineMsg(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
