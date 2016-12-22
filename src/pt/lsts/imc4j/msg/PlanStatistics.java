package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.util.TupleList;

public class PlanStatistics extends Message {
	public static final int ID_STATIC = 564;

	/**
	 * The name of the plan to be generated.
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String plan_id = "";

	/**
	 * Type of plan statistics, if they are launched before, during or after the plan execution.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public TYPE type = TYPE.values()[0];

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<PROPERTIES> properties = EnumSet.noneOf(PROPERTIES.class);

	/**
	 * Maneuver and plan duration statistics in seconds, for example: “Total=1000,Goto1=20,Rows=980”
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList durations = new TupleList("");

	/**
	 * Distances travelled in meters in each maneuver and/or total: “Total=2000,Rows=1800,Elevator=200”
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList distances = new TupleList("");

	/**
	 * List of components active by plan actions during the plan and time active in seconds: “Sidescan=100,Camera Module=150”
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList actions = new TupleList("");

	/**
	 * Amount of fuel spent, in battery percentage, by different parcels (if applicable): “Total=35,Hotel=5,Payload=10,Motion=20,IMU=0”
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT,
			units = "TupleList"
	)
	public TupleList fuel = new TupleList("");

	public String abbrev() {
		return "PlanStatistics";
	}

	public int mgid() {
		return 564;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, plan_id);
			_out.writeByte((int)(type != null? type.value() : 0));
			long _properties = 0;
			if (properties != null) {
				for (PROPERTIES __properties : properties.toArray(new PROPERTIES[0])) {
					_properties += __properties.value();
				}
			}
			_out.writeByte((int)_properties);
			SerializationUtils.serializePlaintext(_out, durations == null? null : durations.toString());
			SerializationUtils.serializePlaintext(_out, distances == null? null : distances.toString());
			SerializationUtils.serializePlaintext(_out, actions == null? null : actions.toString());
			SerializationUtils.serializePlaintext(_out, fuel == null? null : fuel.toString());
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			plan_id = SerializationUtils.deserializePlaintext(buf);
			type = TYPE.valueOf(buf.get() & 0xFF);
			long properties_val = buf.get() & 0xFF;
			properties.clear();
			for (PROPERTIES PROPERTIES_op : PROPERTIES.values()) {
				if ((properties_val & PROPERTIES_op.value()) == PROPERTIES_op.value()) {
					properties.add(PROPERTIES_op);
				}
			}
			durations = new TupleList(SerializationUtils.deserializePlaintext(buf));
			distances = new TupleList(SerializationUtils.deserializePlaintext(buf));
			actions = new TupleList(SerializationUtils.deserializePlaintext(buf));
			fuel = new TupleList(SerializationUtils.deserializePlaintext(buf));
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum TYPE {
		TP_PREPLAN(0l),

		TP_INPLAN(1l),

		TP_POSTPLAN(2l);

		protected long value;

		TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static TYPE valueOf(long value) throws IllegalArgumentException {
			for (TYPE v : TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for TYPE: "+value);
		}
	}

	public enum PROPERTIES {
		PRP_BASIC(0x00l),

		PRP_NONLINEAR(0x01l),

		PRP_INFINITE(0x02l),

		PRP_CYCLICAL(0x04l),

		PRP_ALL(0x07l);

		protected long value;

		PROPERTIES(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static PROPERTIES valueOf(long value) throws IllegalArgumentException {
			for (PROPERTIES v : PROPERTIES.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for PROPERTIES: "+value);
		}
	}
}
