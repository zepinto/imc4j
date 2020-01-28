package pt.lsts.imc4j.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;

public class LsfReader {

	private SeekableByteChannel channel;
	private ConcurrentHashMap<Class<? extends Message>, ArrayList<Consumer<? extends Message>>> consumers = new ConcurrentHashMap<>();

	public LsfReader(File input) throws IOException {
		channel = Files.newByteChannel(input.toPath(), StandardOpenOption.READ);
	}

	public <M extends Message> void bind(Class<M> msg, Consumer<M> consumer) {
		synchronized (consumers) {
			consumers.putIfAbsent(msg, new ArrayList<>());
			
			consumers.get(msg).add(consumer);
		}
	}
	
	@SuppressWarnings("unchecked")
	<M extends Message> void process(M m) {
		consumers.getOrDefault(m.getClass(), new ArrayList<>()).forEach(c -> ((Consumer<M>)c).accept(m));
	}
	
	public void process() throws IOException {
		Message m = null;
		while ((m = nextMessage()) != null) {
			process(m);
		}
	}

	public Message nextMessage() throws IOException {
		long pos = channel.position();
		ByteBuffer header = ByteBuffer.allocate(20);
		int read = channel.read(header);
		if (read < 20) {
			channel.position(pos);
			return null;
		}
		header.position(0);
		short s = header.getShort();
		if (s != Message.SYNC_WORD) {
			if (s == Short.reverseBytes(Message.SYNC_WORD))
				header.order(ByteOrder.LITTLE_ENDIAN);
			else
				throw new IOException("Invalid Synchronization number");
		}

		int mgid = header.getShort() & 0xFFFF;
		Message m = MessageFactory.create(mgid);
		if (m == null)
			return null;

		int size = header.getShort();
		m.timestamp = header.getDouble();
		m.src = header.getShort() & 0xFFFF;
		m.src_ent = header.get() & 0xFF;
		m.dst = header.getShort() & 0xFFFF;
		m.dst_ent = header.get() & 0xFF;
		ByteBuffer payload = ByteBuffer.allocate(size + 2);
		channel.read(payload);
		payload.position(0);
		payload.order(header.order());
		m.deserializeFields(payload);

		return m;
	}

	public void close() throws IOException {
		channel.close();
	}

	public static void main(String[] args) throws IOException {
		LsfReader reader = new LsfReader(new File("Data.lsf"));
		reader.bind(EstimatedState.class, System.out::println);
		reader.process();
	}
}
