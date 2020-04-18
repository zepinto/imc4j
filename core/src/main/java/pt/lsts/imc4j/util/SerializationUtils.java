package pt.lsts.imc4j.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;

public class SerializationUtils {

	private static final int[] crc_table = { 0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241, 0xC601,
			0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440, 0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1,
			0xCE81, 0x0E40, 0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841, 0xD801, 0x18C0, 0x1980,
			0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40, 0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
			0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641, 0xD201, 0x12C0, 0x1380, 0xD341, 0x1100,
			0xD1C1, 0xD081, 0x1040, 0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240, 0x3600, 0xF6C1,
			0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441, 0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80,
			0xFE41, 0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840, 0x2800, 0xE8C1, 0xE981, 0x2940,
			0xEB01, 0x2BC0, 0x2A80, 0xEA41, 0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40, 0xE401,
			0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640, 0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0,
			0x2080, 0xE041, 0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240, 0x6600, 0xA6C1, 0xA781,
			0x6740, 0xA501, 0x65C0, 0x6480, 0xA441, 0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
			0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840, 0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01,
			0x7BC0, 0x7A80, 0xBA41, 0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40, 0xB401, 0x74C0,
			0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640, 0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080,
			0xB041, 0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241, 0x9601, 0x56C0, 0x5780, 0x9741,
			0x5500, 0x95C1, 0x9481, 0x5440, 0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40, 0x5A00,
			0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841, 0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1,
			0x8A81, 0x4A40, 0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41, 0x4400, 0x84C1, 0x8581,
			0x4540, 0x8701, 0x47C0, 0x4680, 0x8641, 0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040, };

	public static Message deserializeMessage(byte[] data) throws Exception {
		ByteBuffer buf = ByteBuffer.wrap(data);
		short s = buf.getShort();
		if (s != Message.SYNC_WORD) {
			if (s == Short.reverseBytes(Message.SYNC_WORD))
				buf.order(ByteOrder.LITTLE_ENDIAN);			
			else
				throw new Exception("Invalid Synchronization number");
		}
		
		int mgid = buf.getShort() & 0xFFFF;
		Message m = MessageFactory.create(mgid);
		if (m == null)
			return null;
		
		//size
		buf.getShort();
		
		m.timestamp = buf.getDouble();
		m.src = buf.getShort() & 0xFFFF;
		m.src_ent = buf.get() & 0xFF;
		m.dst = buf.getShort() & 0xFFFF;
		m.dst_ent = buf.get() & 0xFF;
		m.deserializeFields(buf);
		return m;
	}
	
	public static Message deserializeMessage(InputStream input) throws Exception {
		if (input.available() < 20) {
			return null;
		}
		byte[] header = new byte[20];
		input.read(header);		
		
		ByteBuffer buf = ByteBuffer.wrap(header);
		short s = buf.getShort();
		if (s != Message.SYNC_WORD) {
			if (s == Short.reverseBytes(Message.SYNC_WORD))
				buf.order(ByteOrder.LITTLE_ENDIAN);			
			else
				throw new Exception("Invalid Synchronization number: "+String.format("%X", s));
		}
		int mgid = buf.getShort() & 0xFFFF;
		Message m = MessageFactory.create(mgid);
		if (m == null)
			return null;
		
		//size
		short size = buf.getShort();
		m.timestamp = buf.getDouble();
		m.src = buf.getShort() & 0xFFFF;
		m.src_ent = buf.get() & 0xFF;
		m.dst = buf.getShort() & 0xFFFF;
		m.dst_ent = buf.get() & 0xFF;
		byte[] remaining = new byte[size+2];
		input.read(remaining);
		ByteBuffer payload = ByteBuffer.wrap(remaining);
		payload.order(buf.order());
		m.deserializeFields(payload);
		return m;
	}
	
	public static byte[] serializeMessage(Message msg) {
		ByteArrayOutputStream message = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(message);
		byte[] payload = msg.serializeFields();
		try {
			out.writeShort(msg.sync);
			out.writeShort(msg.mgid());
			out.writeShort(payload.length);
			out.writeDouble(msg.timestamp);
			out.writeShort(msg.src);
			out.writeByte(msg.src_ent);
			out.writeShort(msg.dst);
			out.writeByte(msg.dst_ent);
			out.write(payload);
			out.writeShort(0);

			// CRC calculation
			byte[] result = message.toByteArray();
			int checksum = crc16(result, 0, result.length - 2);
			ByteArrayOutputStream baos = new ByteArrayOutputStream(2);
			new DataOutputStream(baos).writeShort(checksum);
			byte[] crc = baos.toByteArray();
			result[result.length - 2] = crc[0];
			result[result.length - 1] = crc[1];
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			return new byte[0];
		}		
	}
	
	public static int serializePlaintext(DataOutputStream out, String text) throws IOException {
		if (text == null)
			text = "";
		return serializeRawdata(out, text.getBytes("UTF-8"));
	}
	
	public static String deserializePlaintext(ByteBuffer buf) throws IOException {
		byte[] data = deserializeRawdata(buf);
		return new String(data, Charset.forName("UTF-8"));
	}
	
	public static int serializeRawdata(DataOutputStream out, byte[] data) throws IOException {
		if (data == null)
			data = new byte[0];
		
		out.writeShort(data.length);
		out.write(data);
		return 2 + data.length;
	}
	
	public static byte[] deserializeRawdata(ByteBuffer buf) throws IOException {
		int length = buf.getShort() & 0xFFFF;
		byte[] data = new byte[length];
		buf.get(data);
		return data;
	}
	
	public static int serializeInlineMsg(DataOutputStream out, Message msg) throws IOException {
		if (msg == null) {
			out.writeShort(65535);
			return 2;
		}
		
		byte[] data = msg.serializeFields(); 
		out.writeShort(msg.mgid());
		out.write(data);
		return 2 + data.length;
	}
	
	public static <T extends Message> T deserializeInlineMsg(ByteBuffer buf) throws IOException {
		int mgid = buf.getShort() & 0xFFFF;
		if (mgid == 0)
			return null;
		
		@SuppressWarnings("unchecked")
		T msg = (T) MessageFactory.create(mgid);
		
		if (msg == null)
			return null;
		
		msg.deserializeFields(buf);
		
		return msg;		
	}

	public static int serializeMsgList(DataOutputStream out, ArrayList<? extends Message> msgs) throws IOException {
		if (msgs == null || msgs.isEmpty()) {
			out.writeShort(0);
			return 2;
		}
		
		out.writeShort(msgs.size());
		int count = 2;
		for (Message m : msgs)
			count += serializeInlineMsg(out, m);
		
		return count;			
	}

	@SuppressWarnings("unchecked")
	public static <T extends Message> ArrayList<T> deserializeMsgList(ByteBuffer buf) throws IOException {
		ArrayList<T> msgs = new ArrayList<>();
		int numMsgs = buf.getShort() & 0xFFFF;

		for (int i = 0; i < numMsgs; i++) {
			msgs.add((T)deserializeInlineMsg(buf));
		}
		
		return msgs;		
	}

	public static int crc16(byte[] bytes, int offset, int length) {
		int crc = 0x0000;
		for (int i = offset; i < offset + length; i++) {
			crc = (crc >>> 8) ^ crc_table[(crc ^ bytes[i]) & 0xff];
		}
		return crc;
	}
}
