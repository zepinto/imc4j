package pt.lsts.imc.net;

import java.util.ArrayList;

import pt.lsts.imc.msg.Message;

public abstract class ImcPoller {

	public abstract boolean accepts(Message msg);

	private ArrayList<Message> msgs = new ArrayList<>();
	private AbstractImcConnection conn;
	boolean firstMessage = true;
	
	public ImcPoller(AbstractImcConnection conn) {
		this.conn = conn;
	}

	public static ImcPoller any(AbstractImcConnection conn) {
		return new ImcPoller(conn) {
			@Override
			public boolean accepts(Message msg) {
				return msg != null;
			}
		};
	}
	
	public static ImcPoller all(AbstractImcConnection conn) {
		ImcPoller ret = any(conn);
		ret.firstMessage = false;
		return ret;
	}
	
	public static ImcPoller all(AbstractImcConnection conn, final String abbrev) {
		ImcPoller ret = ofType(conn, abbrev);
		ret.firstMessage = false;
		return ret;
	}
	
	public static ImcPoller ofType(AbstractImcConnection conn, final String abbrev) {
		return new ImcPoller(conn) {
			@Override
			public boolean accepts(Message msg) {
				return msg != null && msg.getClass().getSimpleName().equals(abbrev);
			}
		};
	}

	public static ImcPoller ofSource(AbstractImcConnection conn, String src) {
		final int srcId = IMCRegistry.resolveSystem(src);
		return new ImcPoller(conn) {
			@Override
			public boolean accepts(Message msg) {
				return msg != null && msg.src == srcId;
			}
		};
	}

	public static ImcPoller ofTypeAndSource(AbstractImcConnection conn, final String abbrev, String src) {
		final Integer srcId = IMCRegistry.resolveSystem(src);
		return new ImcPoller(conn) {
			@Override
			public boolean accepts(Message msg) {
				return msg != null && srcId == msg.src && msg.getClass().getSimpleName().equals(abbrev);
			}
		};
	}

	public boolean offer(Message msg) {
		if (accepts(msg)) {
			synchronized (msgs) {
				msgs.add(msg);
				msgs.notify();
			}
			return true;
		}

		return false;
	}
	
	public Message poll(long timeout) {
		synchronized (msgs) {
			msgs.clear();
		}
		Message ret = null;
		conn.addPoller(this);
		
		synchronized (msgs) {
			try {
				msgs.wait(timeout);
			} catch (Exception e) {

			}
			if (!msgs.isEmpty())
				ret = msgs.get(0);						
		}

		conn.removePoller(this);
		return ret;
	}

	public Message[] all(long timeout) {
		synchronized (msgs) {
			msgs.clear();
		}
		conn.addPoller(this);
		try {
			Thread.sleep(timeout);
		}
		catch (Exception e) {
		}
		conn.removePoller(this);
		synchronized (msgs) {
			return msgs.toArray(new Message[0]);	
		}		
	}
}
