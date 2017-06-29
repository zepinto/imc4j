package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

public class VehicleDepot extends Message {
	public static final int ID_STATIC = 913;

	/**
	 * IMC Id of the vehicle that this depot belongs to.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int vehicle = 0;

	/**
	 * Latitude of the depot location.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lat = 0;

	/**
	 * Longitude of the depot location.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lon = 0;

	/**
	 * Time in seconds, when the vehicle must have returned to the depot.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double deadline = 0;

	public String abbrev() {
		return "VehicleDepot";
	}

	public int mgid() {
		return 913;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeShort(vehicle);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeDouble(deadline);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			vehicle = buf.getShort() & 0xFFFF;
			lat = buf.getDouble();
			lon = buf.getDouble();
			deadline = buf.getDouble();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
