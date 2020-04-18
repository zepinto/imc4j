package pt.lsts.imc4j.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;

/**
 * Presence of DMS (Dimethyl Sulphide).
 * If the value of the channel is greater than zero, it means DMS was detected.
 */
public class DmsDetection extends Message {
	public static final int ID_STATIC = 908;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch01 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch02 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch03 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch04 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch05 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch06 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch07 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch08 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch09 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch10 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch11 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch12 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch13 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch14 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch15 = 0f;

	@FieldType(
			type = IMCField.TYPE_FP32
	)
	public float ch16 = 0f;

	public String abbrev() {
		return "DmsDetection";
	}

	public int mgid() {
		return 908;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeFloat(ch01);
			_out.writeFloat(ch02);
			_out.writeFloat(ch03);
			_out.writeFloat(ch04);
			_out.writeFloat(ch05);
			_out.writeFloat(ch06);
			_out.writeFloat(ch07);
			_out.writeFloat(ch08);
			_out.writeFloat(ch09);
			_out.writeFloat(ch10);
			_out.writeFloat(ch11);
			_out.writeFloat(ch12);
			_out.writeFloat(ch13);
			_out.writeFloat(ch14);
			_out.writeFloat(ch15);
			_out.writeFloat(ch16);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			ch01 = buf.getFloat();
			ch02 = buf.getFloat();
			ch03 = buf.getFloat();
			ch04 = buf.getFloat();
			ch05 = buf.getFloat();
			ch06 = buf.getFloat();
			ch07 = buf.getFloat();
			ch08 = buf.getFloat();
			ch09 = buf.getFloat();
			ch10 = buf.getFloat();
			ch11 = buf.getFloat();
			ch12 = buf.getFloat();
			ch13 = buf.getFloat();
			ch14 = buf.getFloat();
			ch15 = buf.getFloat();
			ch16 = buf.getFloat();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
}
