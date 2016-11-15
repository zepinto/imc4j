package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.def.OpLimitsMask;

/**
 * Definition of operational limits.
 */
public class OperationalLimits extends Message {
	public static final int ID_STATIC = 504;

	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<OpLimitsMask> mask = EnumSet.noneOf(OpLimitsMask.class);

	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float max_depth = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float min_altitude = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m"
	)
	public float max_altitude = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m/s"
	)
	public float min_speed = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m/s"
	)
	public float max_speed = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			min = 0,
			units = "m/s"
	)
	public float max_vrate = 0f;

	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 1.5707963267948966,
			min = -1.5707963267948966,
			units = "rad"
	)
	public double lat = 0;

	@FieldType(
			type = IMCField.TYPE_FP64,
			max = 3.141592653589793,
			min = -3.141592653589793,
			units = "rad"
	)
	public double lon = 0;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "rad"
	)
	public float orientation = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float width = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32,
			units = "m"
	)
	public float length = 0f;

	public int mgid() {
		return 504;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			long _mask = 0;
			if (mask != null) {
				for (OpLimitsMask __mask : mask.toArray(new OpLimitsMask[0])) {
					_mask += __mask.value();
				}
			}
			_out.writeByte((int)_mask);
			_out.writeFloat(max_depth);
			_out.writeFloat(min_altitude);
			_out.writeFloat(max_altitude);
			_out.writeFloat(min_speed);
			_out.writeFloat(max_speed);
			_out.writeFloat(max_vrate);
			_out.writeDouble(lat);
			_out.writeDouble(lon);
			_out.writeFloat(orientation);
			_out.writeFloat(width);
			_out.writeFloat(length);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			long mask_val = buf.get() & 0xFF;
			mask.clear();
			for (OpLimitsMask OpLimitsMask_op : OpLimitsMask.values()) {
				if ((mask_val & OpLimitsMask_op.value()) == OpLimitsMask_op.value()) {
					mask.add(OpLimitsMask_op);
				}
			}
			max_depth = buf.getFloat();
			min_altitude = buf.getFloat();
			max_altitude = buf.getFloat();
			min_speed = buf.getFloat();
			max_speed = buf.getFloat();
			max_vrate = buf.getFloat();
			lat = buf.getDouble();
			lon = buf.getDouble();
			orientation = buf.getFloat();
			width = buf.getFloat();
			length = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
