package pt.lsts.imc.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.AttributeStorage;

import pt.lsts.imc.msg.Message;
import pt.lsts.imc.msg.MessageFactory;

public class IMCDecoder extends AbstractTransformer<Buffer, Message>{

	protected final Attribute<Boolean> atrBigEndian;
	protected final Attribute<Integer> atrSize;
	protected final Attribute<Message> atrMsg;
	
	public IMCDecoder() {
		atrBigEndian = attributeBuilder.createAttribute("imc.sync");
		atrSize = attributeBuilder.createAttribute("imc.size");
		atrMsg = attributeBuilder.createAttribute("imc.mgid");
	}
	
	@Override
	public String getName() {
		return "IMCDecoder";
	}

	@Override
	public boolean hasInputRemaining(AttributeStorage storage, Buffer input) {
		return input != null && input.hasRemaining();
	}

	@Override
	protected TransformationResult<Buffer, Message> transformImpl(AttributeStorage storage, Buffer input)
			throws TransformationException {
		
		if (input == null)
			throw new TransformationException("Input could not be null");
		
		
		Boolean bigendian = atrBigEndian.get(storage);
		Message msg = atrMsg.get(storage);
		Integer size = atrSize.get(storage);
		
		if (bigendian == null) {
			bigendian = true;
			if (input.remaining() < 22)
				return TransformationResult.createIncompletedResult(input);
			else {
				int sync = input.getShort();
				if (sync != Message.SYNC_WORD) {
					if (sync == Short.reverseBytes(Message.SYNC_WORD)) {
						bigendian = false;
						input.order(ByteOrder.LITTLE_ENDIAN);
					}
					else
						return TransformationResult.createErrorResult(1, String.format("Invalid sync word: %02X.", sync));
				}
			}
			
			int mgid = input.getShort() & 0xFFFF;
			msg = MessageFactory.create(mgid);
			if (msg == null)
				return TransformationResult.createErrorResult(2, String.format("Message id not recognized: %0d.", mgid));
			
			size = input.getShort() & 0xFFFF;
			
			if (input.remaining() < size + (22-6)) {
				atrBigEndian.set(storage, bigendian);
				atrSize.set(storage, size);
				atrMsg.set(storage, msg);
				return TransformationResult.createIncompletedResult(input);
			}
		}
		
		if (input.remaining() < size + (22-6))
			return TransformationResult.createIncompletedResult(input);
			
		if (!bigendian)
			input.order(ByteOrder.LITTLE_ENDIAN);
		
		msg.timestamp = input.getDouble();
		msg.src = input.getShort() & 0xFFFF;
		msg.src_ent = input.get() & 0xFF;
		msg.dst = input.getShort() & 0xFFFF;
		msg.dst_ent = input.get() & 0xFF;
		try {
			ByteBuffer buf = input.toByteBuffer();
			buf.order(input.order());
			msg.deserializeFields(buf);
		}
		catch (IOException e) {
			return TransformationResult.createErrorResult(3, String.format("Error reading message of type "+msg.getClass().getSimpleName()));
		}

		return TransformationResult.createCompletedResult(msg, null);
	}
}
