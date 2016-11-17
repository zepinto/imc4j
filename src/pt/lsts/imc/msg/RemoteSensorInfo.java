package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;
import pt.lsts.imc.util.TupleList;

/**
 * Whenever the CUCS receives a message from one of the existing sensors (through SMS, ZigBee, Acoustic Comms, ...) it disseminates that info recurring to this message.
 */
public class RemoteSensorInfo extends Message {
	public static final int ID_STATIC = 601;

	/**
	 * An unique string that identifies the sensor. Used mostly for logging/presentation.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String id = "";

	/**
	 * The class of a sensor tells the type of sensor originating this message. It will determine how the sensor is to be shown and (optionally) how the custom data (tuplelist) is to be interpreted.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String sensor_class = "";

	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lat = 0;

	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "rad"
	)
	public double lon = 0;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float alt = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float heading = 0f;

	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList data = new TupleList("");

	public int mgid() {
		return 601;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, id);
			SerializationUtils.serializePlaintext(_out, sensor_class);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(alt);
			_out.writeFloat(heading);
			SerializationUtils.serializePlaintext(_out, data == null? null : data.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			id = SerializationUtils.deserializePlaintext(buf);
			sensor_class = SerializationUtils.deserializePlaintext(buf);
			lat = buf.getDouble();
			lon = buf.getDouble();
			alt = buf.getFloat();
			heading = buf.getFloat();
			data = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
