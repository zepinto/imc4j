package pt.lsts.imc.net;

import org.glassfish.grizzly.AbstractTransformer;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.TransformationException;
import org.glassfish.grizzly.TransformationResult;
import org.glassfish.grizzly.attributes.AttributeStorage;

import pt.lsts.imc.msg.Message;

public class IMCEncoder extends AbstractTransformer<Message, Buffer>{

	@Override
	public String getName() {
		return "IMCEncoder";
	}

	@Override
	public boolean hasInputRemaining(AttributeStorage storage, Message input) {
		return input != null;
	}

	@Override
	protected TransformationResult<Message, Buffer> transformImpl(AttributeStorage storage, Message input)
			throws TransformationException {
		
		if (input == null)
			throw new TransformationException("Input could not be null");
		
		byte[] data = input.serialize();
		
		final Buffer output = obtainMemoryManager(storage).allocate(data.length);
		output.put(data);
		output.flip();
		output.allowBufferDispose(true);
		return TransformationResult.createCompletedResult(output, null);		
	}
}
