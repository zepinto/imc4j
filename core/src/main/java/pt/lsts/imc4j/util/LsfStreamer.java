package pt.lsts.imc4j.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;
import pt.lsts.imc4j.msg.SonarData;
import pt.lsts.imc4j.msg.SonarData.TYPE;

public class LsfStreamer<M extends Message> implements AutoCloseable, Iterator<M> {

	private ReadableByteChannel channel;
	private final ByteBuffer header = ByteBuffer.allocate(20);
	private M currentMessage = null;
	private int mgid = -1;

	public static LsfStreamer<Message> of(InputStream in) throws IOException {
		return new LsfStreamer<Message>(in);
	}

	public static <M extends Message> LsfStreamer<M> of(InputStream in, Class<M> type) throws IOException {
		LsfStreamer<M> streamer = new LsfStreamer<M>(in);
		streamer.setType(type);
		return streamer;
	}

	public static <M extends Message> LsfStreamer<M> of(Path file, Class<M> type) throws IOException {
		if (!Files.isDirectory(file)) {
			LsfStreamer<M> streamer = new LsfStreamer<M>(file, LsfStreamer.isGzipped(file));
			streamer.setType(type);
			return streamer;
		} else {
			if (Files.isReadable(file.resolve("Data.lsf"))) {
				return LsfStreamer.of(file.resolve("Data.lsf"), type);
			} else {
				return LsfStreamer.of(file.resolve("Data.lsf.gz"), type);
			}
		}
	}

	public static LsfStreamer<Message> of(Path file) throws IOException {
		if (!Files.isDirectory(file))
			return new LsfStreamer<Message>(file, LsfStreamer.isGzipped(file));
		else {
			if (Files.isReadable(file.resolve("Data.lsf"))) {
				return LsfStreamer.of(file.resolve("Data.lsf"));
			} else {
				return LsfStreamer.of(file.resolve("Data.lsf.gz"));
			}
		}
	}

	private void setType(Class<M> type) {
		try {
			this.mgid = type.getField("ID_STATIC").getInt(null);
		} catch (Exception e) {
			this.mgid = -1;
		}
	}

	private LsfStreamer(InputStream input) throws IOException {
		channel = Channels.newChannel(input);
	}

	private LsfStreamer(Path file, boolean compressed) throws IOException {
		if (!compressed) {
			channel = Files.newByteChannel(file, StandardOpenOption.READ);
		} else {
			channel = Channels.newChannel(new GZIPInputStream(Files.newInputStream(file)));
		}
	}

	private static boolean isGzipped(Path p) {
		return p.getFileName().toString().endsWith(".gz");
	}

	@SuppressWarnings("unchecked")
	private M create(int mgid) {
		try {
			return (M) MessageFactory.create(mgid);	
		}
		catch (Exception e) {
			return null;
		}		
	}

	public M nextMessage(int desiredId) throws IOException {

		M msg = null;

		do {
			channel.read(header);
			if (header.position() < 20) {
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
			int readId = header.getShort() & 0xFFFF;
			int size = header.getShort();

			if (desiredId != -1 && readId != desiredId) {
				ByteBuffer payload = ByteBuffer.allocate(size + 2);
				channel.read(payload);
				header.position(0);
			} else {				
				msg = create(readId);
				msg.timestamp = header.getDouble();
				msg.src = header.getShort() & 0xFFFF;
				msg.src_ent = header.get() & 0xFF;
				msg.dst = header.getShort() & 0xFFFF;
				msg.dst_ent = header.get() & 0xFF;
				ByteBuffer payload = ByteBuffer.allocate(size + 2);
				channel.read(payload);
				payload.position(0);
				payload.order(header.order());
				msg.deserializeFields(payload);
				header.position(0);
				
			}
		} while (msg == null);

		return msg;
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public boolean hasNext() {
		if (currentMessage == null) {
			try {
				currentMessage = nextMessage(mgid);
			} catch (Exception e) {
				return false;
			}
		}
		return currentMessage != null;
	}

	@Override
	public M next() {
		M next = currentMessage;
		currentMessage = null;
		return next;
	}

	public Stream<M> stream() {
		return StreamSupport.stream(() -> Spliterators.spliteratorUnknownSize(this, 0), 0, false);
	}

	public void forEach(Class<M> clazz, Consumer<M> consumer) {
		stream().filter(clazz::isInstance).map(clazz::cast).forEach(consumer);
	}

	public void forEach(Consumer<Message> consumer) {
		stream().forEach(consumer);
	}

	public static void main(String[] args) throws IOException {
		// header
		System.out.println("Timestamp,Frequency,MinimumRange,MaximumRange,NumSamples,[Data...]");
		
		// stream messages of type SonarData
		LsfStreamer.of(Path.of("/home/zp/Desktop/JavaTests/Data.lsf.gz"), SonarData.class).stream()		
				// filter by type
				.filter(m -> m.type == TYPE.ST_ECHOSOUNDER)				
				.forEach(m -> {
					Instant i = Instant.ofEpochMilli((long)(m.timestamp * 1000));
					System.out.print(i+","+m.frequency+","+m.min_range+","+m.max_range+","+m.data.length);
					for (int b = 0; b < m.data.length; b++) {
						System.out.print(","+(m.data[b]&0xFF));
					}
					System.out.println();
				});
	}
}