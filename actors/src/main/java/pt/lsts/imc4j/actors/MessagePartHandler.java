package pt.lsts.imc4j.actors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessagePart;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;

public class MessagePartHandler extends AbstractActor {

	private LinkedHashMap<Integer, ArrayList<MessagePart>> incoming = new LinkedHashMap<Integer, ArrayList<MessagePart>>();
	
	public MessagePartHandler(ActorContext context) {
		super(context);
	}

	@Subscribe
	@Publish(Message.class)
	public void on(MessagePart fragment) {
		int hash = (fragment.src + "" + fragment.uid).hashCode();
		if (!incoming.containsKey(hash)) {
			incoming.put(hash, new ArrayList<MessagePart>());
		}
		incoming.get(hash).add(fragment);
		if (incoming.get(hash).size() >= fragment.num_frags) {
			ArrayList<MessagePart> parts = incoming.get(hash);
			incoming.remove(hash);
			try {
				Message m = reassemble(parts);
				post(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Message reassemble(List<MessagePart> parts) throws Exception {
		Collections.sort(parts, new Comparator<MessagePart>() {
			@Override
			public int compare(MessagePart o1, MessagePart o2) {
				return o1.frag_number - o2.frag_number;
			}
		});

		int totalSize = 0;
		for (MessagePart p : parts) {
			totalSize += p.data.length;
		}
		byte[] res = new byte[totalSize];
		int pos = 0;
		for (MessagePart p : parts) {
			System.arraycopy(p.data, 0, res, pos, p.data.length);
			pos += p.data.length;
		}

		return Message.deserialize(res);
	}
	
}
