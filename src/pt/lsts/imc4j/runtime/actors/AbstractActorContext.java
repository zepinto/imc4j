package pt.lsts.imc4j.runtime.actors;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EntityList;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.callbacks.PeriodicScheduler;
import pt.lsts.imc4j.runtime.clock.ActorClock;
import pt.lsts.imc4j.runtime.clock.RealTimeClock;
import pt.lsts.imc4j.runtime.state.IMCQuery;
import pt.lsts.imc4j.runtime.state.IMCRegistry;
import pt.lsts.imc4j.runtime.state.IMCState;

public abstract class AbstractActorContext extends BaseFilter implements ActorContext {

	private Bus bus = new Bus(ThreadEnforcer.ANY);
	private HashSet<Object> listeners = new HashSet<>();
	private IMCRegistry registry = new IMCRegistry();
	private RealTimeClock clock = new RealTimeClock();
	protected ExecutorService executor = Executors.newFixedThreadPool(3);
	private PeriodicScheduler scheduler = new PeriodicScheduler(bus);
	private IMCState state = new IMCState(this);
	
	@Override
	public NextAction handleRead(FilterChainContext ctx) throws IOException {
		Message msg = ctx.getMessage();
		if (msg.mgid() == Announce.ID_STATIC)
			registry.setAnnounce((Announce) msg, (InetSocketAddress) ctx.getAddress());
		else if (msg.mgid() == EntityList.ID_STATIC)
			registry.setEntityList((EntityList) msg);

		handleMessage(msg);

		return ctx.getStopAction();
	}

	public void handleMessage(Message msg) {
		bus.post(msg); 
		
	}	
	
	@Override
	public void post(Message msg) {
		fillIn(msg);
		bus.post(msg);
	}

	protected void fillIn(Message msg) {
		if (msg.src == 0xFFFF)
			msg.src = registry.getImcId();
	}
	
	public int send(Message msg) throws Exception {
		int count = 0;
		for (String peer : registry().peers()) {
			try {
				send(msg, peer);
				count++;
			}
			catch (Exception e) {
				
			}			
		}
		return count;
	}
	
	public void reply(Message request, Message reply) throws Exception {
		String name = registry.resolveSystem(request.src);
		
		if (name == null)
			throw new Exception("Requester has not announced yet");
			
		reply.dst_ent = request.src_ent;
		reply.dst = request.src;
		send(reply, name);
	}

	@Override
	public Future<Boolean> deliver(Message msg, String destination) {
		Callable<Boolean> callable = new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				send(msg, destination);
				return true;							
			}
		};
		return executor.submit(callable);
	}

	@Override
	public int register(AbstractActor actor, String name) {
		synchronized (listeners) {
			if (listeners.add(actor)) {
				bus.register(actor);
				scheduler.register(actor);
			}
		}
		return registry.registerLocalEntity(name);
	}

	@Override
	public void unregister(AbstractActor actor) {
		unreg(actor);
	}
	
	private void unreg(Object pojo) {
		bus.unregister(pojo);
		scheduler.unregister(pojo);
		synchronized (listeners) {
			listeners.remove(pojo);
		}
	}

	@Override
	public ActorClock clock() {
		return clock;
	}

	@Override
	public IMCRegistry registry() {
		return registry;
	}
	
	@Override
	public <T extends Message> IMCQuery<T> query(Class<T> clazz) {
		return state.q(clazz);
	}
	
	@Override
	public IMCQuery<Message> query(String abbrev) {
		return state.q(abbrev);
	}
	
	@Override
	public void start() {
		try {
			onStart();
		}
		catch (Exception e) {
			e.printStackTrace();
		}			
	}
	
	@Override
	public void stop() {
		try {
			onStop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		scheduler.stopAll();
		synchronized (listeners) {
			for (Object o : listeners) {
				unreg(o);
			}
			listeners.clear();
		}
	}

	public abstract void onStart() throws Exception ;
	
	public abstract void onStop() throws Exception;
	
	public abstract void send(Message msg, String destination) throws Exception;
	
	
}
