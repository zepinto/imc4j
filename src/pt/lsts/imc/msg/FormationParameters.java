package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;
import pt.lsts.imc.util.TupleList;

/**
 * A "Formation" is defined by the relative positions of the vehicles
 * inside the formation, and the reference frame where this positions are defined.
 * The formation reference frame may be:
 * - Earth Fixed: Where the vehicles relative position do not depend on the followed path.
 * This results in all UAVs following the same path with an offset relative to each other;
 * - Path Fixed:  Where the vehicles relative position depends on the followed path,
 * changing the inter-vehicle offset direction with the path direction.
 * - Path Curved:  Where the vehicles relative position depends on the followed path,
 * changing the inter-vehicle offset direction with the path direction and direction
 * change rate.
 * An offset in the xx axis results in a distance over the curved path line.
 * An offset in the yy axis results in an offset of the vehicle path line relative to the
 * formation center path line.
 */
public class FormationParameters extends Message {
	public static final int ID_STATIC = 476;

	/**
	 * Name of the formation configuration.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String formation_name = "";

	/**
	 * Formation reference frame
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public REFERENCE_FRAME reference_frame = REFERENCE_FRAME.values()[0];

	/**
	 * List of formation participants.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<VehicleFormationParticipant> participants = new ArrayList<>();

	/**
	 * Custom settings for the formation configuration.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList custom = new TupleList("");

	public String abbrev() {
		return "FormationParameters";
	}

	public int mgid() {
		return 476;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, formation_name);
			_out.writeByte((int)(reference_frame != null? reference_frame.value() : 0));
			SerializationUtils.serializeMsgList(_out, participants);
			SerializationUtils.serializePlaintext(_out, custom == null? null : custom.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			formation_name = SerializationUtils.deserializePlaintext(buf);
			reference_frame = REFERENCE_FRAME.valueOf(buf.get() & 0xFF);
			participants = SerializationUtils.deserializeMsgList(buf);
			custom = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum REFERENCE_FRAME {
		OP_EARTH_FIXED(0l),

		OP_PATH_FIXED(1l),

		OP_PATH_CURVED(2l);

		protected long value;

		REFERENCE_FRAME(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static REFERENCE_FRAME valueOf(long value) throws IllegalArgumentException {
			for (REFERENCE_FRAME v : REFERENCE_FRAME.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for REFERENCE_FRAME: "+value);
		}
	}
}
