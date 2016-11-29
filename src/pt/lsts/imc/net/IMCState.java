package pt.lsts.imc.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.msg.Message;
import pt.lsts.imc.msg.MessageFactory;

public class IMCState {

	private LinkedHashMap<Integer, LinkedHashMap<Integer, Message>> state = new LinkedHashMap<>();
	private TimestampComparator timeComparator = new TimestampComparator();

	private int hash(Message msg) {
		return (msg.src << 2) | msg.mgid();
	}

	@Subscribe
	public void on(Message msg) {
		int hash = hash(msg);
		synchronized (state) {
			LinkedHashMap<Integer, Message> msgs = state.get(hash);

			if (msgs == null) {
				msgs = new LinkedHashMap<>();
				state.put(hash, msgs);
			}
			if (msgs.containsKey(msg.src_ent) && msgs.get(msg.src_ent).timestamp > msg.timestamp)
				return;
			msgs.put(msg.src_ent, msg);
		}
	}

	public Collection<Message> get(int src, int mgid) {
		int hash = (src << 2) | mgid;
		synchronized (state) {
			return state.containsKey(hash) ? state.get(hash).values() : new ArrayList<>();
		}
	}

	
	@SuppressWarnings("unchecked")
	public <T extends Message> T get(int src, Class<T> type) {
		int mgid = MessageFactory.idOf(type.getSimpleName());
		return (T) last(src, mgid);		
	}
	
	public Message last(int src, int mgid) {
		ArrayList<Message> msgs = new ArrayList<>();
		msgs.addAll(get(src, mgid));
		if (msgs.isEmpty())
			return null;
		msgs.sort(timeComparator);
		return msgs.iterator().next();
	}

	public Message get(int src, int mgid, int ent) {
		int hash = (src << 2) | mgid;
		synchronized (state) {
			return state.containsKey(hash) ? state.get(hash).get(ent) : null;
		}
	}
	
	public Message get(String source, String abbrev, String entity) {
		int src = IMCRegistry.resolveSystem(source);
		int mgid = MessageFactory.idOf(abbrev);
		int ent = IMCRegistry.resolveEntity(source, entity);
		return get(src, mgid, ent);
	}

	public Message get(String source, String abbrev) {
		int src = IMCRegistry.resolveSystem(source);
		int mgid = MessageFactory.idOf(abbrev);
		return last(src, mgid);
	}

	static class TimestampComparator implements Comparator<Message> {
		@Override
		public int compare(Message o1, Message o2) {
			return new Double(o2.timestamp).compareTo(o1.timestamp);
		}
	}
}
