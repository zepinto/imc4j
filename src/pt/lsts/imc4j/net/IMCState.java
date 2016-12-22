package pt.lsts.imc4j.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.MessageFactory;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;

public class IMCState extends AbstractActor {

	public IMCState(ActorContext context) {
		super(context);
	}

	/**
	 * (src|type) -> (ent -> msg)  
	 */
	private LinkedHashMap<Integer, LinkedHashMap<Integer, Message>> state = new LinkedHashMap<>();
	private TimestampComparator timeComparator = new TimestampComparator();

	private int hash(Message msg) {
		return (msg.src << 16) | msg.mgid();
	}

	public <T extends Message> IMCQuery<T> q(Class<T> clazz) {
		return IMCQuery.q(this, clazz);
	}

	public IMCQuery<Message> q(String abbrev) {
		return IMCQuery.q(this, abbrev);
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
		int hash = (src << 16) | mgid;
		synchronized (state) {
			return state.containsKey(hash) ? state.get(hash).values() : new ArrayList<>();
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Message> T get(int src, Class<T> type) {
		int mgid = MessageFactory.idOf(type.getSimpleName());
		return (T) last(src, mgid);
	}

	public Message ofType(String abbrev) {
		int id = MessageFactory.idOf(abbrev);

		ArrayList<Message> msgs = new ArrayList<>();
		
		synchronized (state) {
			for (int hash : state.keySet()) {				
				if ((hash & 0xFFFF) == id)
					msgs.addAll(state.get(hash).values());
			}
		}

		msgs.sort(timeComparator);
		return msgs.isEmpty() ? null : msgs.get(0);
	}

	public Message last(int src, int mgid) {
		ArrayList<Message> msgs = new ArrayList<>();
		msgs.addAll(get(src, mgid));
		if (msgs.isEmpty())
			return null;
		msgs.sort(timeComparator);
		return msgs.get(0);
	}

	public Message get(int src, int mgid, int ent) {
		int hash = (src << 16) | mgid;
		synchronized (state) {
			return state.containsKey(hash) ? state.get(hash).get(ent) : null;
		}
	}

	public Message get(String source, String abbrev, String entity) {
		Integer src = systemId(source);
		int mgid = MessageFactory.idOf(abbrev);
		Integer ent = entityId(source, entity);
		if (src == null || ent == null || mgid < 0)
			return null;

		return get(src, mgid, ent);
	}

	public Message getFromEntity(String entity, String abbrev) {
		int mgid = MessageFactory.idOf(abbrev);
		ArrayList<Integer> srcs = new ArrayList<>();

		synchronized (state) {
			srcs.addAll(state.keySet());
		}

		ArrayList<Message> candidates = new ArrayList<>();

		for (int i = 0; i < srcs.size(); i++) {
			int src = srcs.get(i) >> 2;
			String sysName = systemName(src);
			if (sysName == null)
				continue;
			int ent = entityId(sysName, entity);
			Message msg = get(src, mgid, ent);
			if (msg != null)
				candidates.add(msg);
		}
		if (candidates.isEmpty())
			return null;

		candidates.sort(timeComparator);
		return candidates.get(0);
	}

	public Message get(String source, String abbrev) {
		Integer src = systemId(source);
		int mgid = MessageFactory.idOf(abbrev);
		if (src == null || mgid < 0)
			return null;
		return last(src, mgid);
	}

	static class TimestampComparator implements Comparator<Message> {
		@Override
		public int compare(Message o1, Message o2) {
			return new Double(o2.timestamp).compareTo(o1.timestamp);
		}
	}
}
