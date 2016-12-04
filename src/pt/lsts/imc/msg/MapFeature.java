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

/**
 * A feature to appear on the map
 */
public class MapFeature extends Message {
	public static final int ID_STATIC = 603;

	/**
	 * The unique identifier for this feature (used as the name for points of interest)
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String id = "";

	/**
	 * The type of feature
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public FEATURE_TYPE feature_type = FEATURE_TYPE.values()[0];

	/**
	 * The red component of the color for this point
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int rgb_red = 0;

	/**
	 * The green component of the color for this point
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int rgb_green = 0;

	/**
	 * The blue component of the color for this point
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int rgb_blue = 0;

	/**
	 * The enclosing feature definition.
	 */
	@FieldType(
			type = IMCField.TYPE_MESSAGELIST
	)
	public ArrayList<MapPoint> feature = new ArrayList<>();

	public String abbrev() {
		return "MapFeature";
	}

	public int mgid() {
		return 603;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, id);
			_out.writeByte((int)(feature_type != null? feature_type.value() : 0));
			_out.writeByte(rgb_red);
			_out.writeByte(rgb_green);
			_out.writeByte(rgb_blue);
			SerializationUtils.serializeMsgList(_out, feature);
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
			feature_type = FEATURE_TYPE.valueOf(buf.get() & 0xFF);
			rgb_red = buf.get() & 0xFF;
			rgb_green = buf.get() & 0xFF;
			rgb_blue = buf.get() & 0xFF;
			feature = SerializationUtils.deserializeMsgList(buf);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum FEATURE_TYPE {
		FTYPE_POI(0l),

		FTYPE_FILLEDPOLY(1l),

		FTYPE_CONTOUREDPOLY(2l),

		FTYPE_LINE(3l),

		FTYPE_TRANSPONDER(4l),

		FTYPE_STARTLOC(5l),

		FTYPE_HOMEREF(6l);

		protected long value;

		FEATURE_TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static FEATURE_TYPE valueOf(long value) throws IllegalArgumentException {
			for (FEATURE_TYPE v : FEATURE_TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for FEATURE_TYPE: "+value);
		}
	}
}
