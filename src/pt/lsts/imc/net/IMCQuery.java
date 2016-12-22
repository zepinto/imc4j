package pt.lsts.imc.net;

import pt.lsts.imc.msg.Message;

/**
 * Created by zp on 30-11-2016.
 */
public class IMCQuery<T> {

	private String source = null;
	private String entity = null;
	private String msgName;
	private IMCState state;

	private IMCQuery(IMCState state) {
		this.state = state;
	}

	public static IMCQuery<Message> q(IMCState state, String abbrev) {
		IMCQuery<Message> q = new IMCQuery<>(state);
		q.msgName = abbrev;
		return q;
	}

	public static <T extends Message> IMCQuery<T> q(IMCState state, Class<T> clazz) {
		IMCQuery<T> q = new IMCQuery<>(state);
		q.msgName = clazz.getSimpleName();
		return q;
	}

	public IMCQuery<T> src(String source) {
		this.source = source;
		return this;
	}

	public IMCQuery<T> ent(String entity) {
		this.entity = entity;
		return this;
	}

	@SuppressWarnings("unchecked")
	public T now() {
		if (source == null && entity == null) {
			return (T) state.ofType(msgName);
		} else if (source != null && entity == null) {
			return (T) state.get(source, msgName);
		} else {
			return (T) state.get(source, msgName, entity);
		}
	}

	public T poll(long millis) {
		long endTime = System.currentTimeMillis() + millis;
		T val = now();

		while (val == null && System.currentTimeMillis() < endTime) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				break;
			}
			val = now();
		}

		return val;
	}

	public static void main(String[] args) {

	}

}
