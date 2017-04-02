package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.util.SerializationUtils;

/**
 * A system description that is to be broadcasted to other systems.
 */
public class Announce extends Message {
	public static final int ID_STATIC = 151;

	/**
	 * System name.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sys_name = "";

	/**
	 * System type.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public SystemType sys_type = SystemType.values()[0];

	/**
	 * The owner IMC system ID.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int owner = 0;

	/**
	 * WGS-84 Latitude. If lat=0 and lon=0 means location value is unknown.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * WGS-84 Longitude. If lat=0 and lon=0 means location value is unknown.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Height above WGS-84 ellipsoid.
	 */
	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float height = 0f;

	/**
	 * Semicolon separated list of URLs. Examples of such URLs are:
	 * - *imc+udp://192.168.106.34:6002/*
	 * - *dune://0.0.0.0/uid/1294925553839635/*
	 * - *http://192.168.106.34/dune/*.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String services = "";

	public String abbrev() {
		return "Announce";
	}

	public int mgid() {
		return 151;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, sys_name);
			_out.writeByte((int)(sys_type != null? sys_type.value() : 0));
			_out.writeShort(owner);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(height);
			SerializationUtils.serializePlaintext(_out, services);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			sys_name = SerializationUtils.deserializePlaintext(buf);
			sys_type = SystemType.valueOf(buf.get() & 0xFF);
			owner = buf.getShort() & 0xFFFF;
			lat = buf.getDouble();
			lon = buf.getDouble();
			height = buf.getFloat();
			services = SerializationUtils.deserializePlaintext(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
