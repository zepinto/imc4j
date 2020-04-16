package pt.lsts.imcwan;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;

public class UnserializedMessage implements Printable {

	public double timestamp = System.currentTimeMillis() / 1000.0;
	public int src = 0xFFFF;
	public int src_ent = 0xFF;
	public int dst = 0xFFFF;
	public int dst_ent = 0xFF;
	public int mgid;
	public int size;
	public byte[] data;
	public boolean bigEndian = true;

	@SuppressWarnings("unchecked")
	public <T extends Message> T get() throws Exception {
		return (T) asMessage();
	}

	public UnserializedMessage(InputStream input) throws IOException {
		byte[] header = new byte[20];
		input.read(header);
		ByteBuffer buf = ByteBuffer.wrap(header);
		short s = buf.getShort();
		if (s != Message.SYNC_WORD) {
			if (s == Short.reverseBytes(Message.SYNC_WORD)) {
				buf.order(ByteOrder.LITTLE_ENDIAN);
				bigEndian = false;
			}
		}
		mgid = buf.getShort() & 0xFFFF;
		size = buf.getShort();
		timestamp = buf.getDouble();
		src = buf.getShort() & 0xFFFF;
		src_ent = buf.get() & 0xFF;
		dst = buf.getShort() & 0xFFFF;
		dst_ent = buf.get() & 0xFF;
		data = Arrays.copyOf(header, 20 + size + 2);
		input.read(data, 20, size + 2);
		
	}

	public UnserializedMessage(byte[] data) {
		this.data = data;
		ByteBuffer buf = ByteBuffer.wrap(data);
		short s = buf.getShort();
		if (s != Message.SYNC_WORD) {
			if (s == Short.reverseBytes(Message.SYNC_WORD)) {
				buf.order(ByteOrder.LITTLE_ENDIAN);
				bigEndian = false;
			}
		}
		mgid = buf.getShort() & 0xFFFF;
		size = buf.getShort();
		timestamp = buf.getDouble();
		src = buf.getShort() & 0xFFFF;
		src_ent = buf.get() & 0xFF;
		dst = buf.getShort() & 0xFFFF;
		dst_ent = buf.get() & 0xFF;
	}

	public byte[] serialize() {
		return Arrays.copyOf(data, 20 + size + 2);
	}

	public Message asMessage() throws Exception {
		Message m = MessageFactory.create(mgid);
		if (m == null)
			return null;

		return Message.deserialize(data);
	}

	@Override
	public String toString() {
		return printObject();
	}
}
