package pt.lsts.imc4j.util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import pt.lsts.imc4j.msg.Message;

public class LsfWriter {

	private SeekableByteChannel channel;
	
	public LsfWriter(File output, boolean append) throws IOException {
		if (append)
			channel = Files.newByteChannel(output.toPath(), StandardOpenOption.APPEND);
		else
			channel = Files.newByteChannel(output.toPath(), StandardOpenOption.CREATE);
	}
	
	public void append(Message msg) throws IOException {
		channel.write(ByteBuffer.wrap(msg.serialize()));
	}
	
	public void close() throws IOException {
		channel.close();
	}
}
