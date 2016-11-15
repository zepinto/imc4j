package pt.lsts.imc.net;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.AbstractCodecFilter;

import pt.lsts.imc.msg.Message;

public class IMCCodec extends AbstractCodecFilter<Buffer, Message> {

	public IMCCodec() {
		super(new IMCDecoder(), new IMCEncoder());
	}

}
