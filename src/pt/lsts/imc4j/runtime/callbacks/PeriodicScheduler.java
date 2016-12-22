package pt.lsts.imc4j.runtime.callbacks;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.squareup.otto.Bus;

import pt.lsts.imc4j.annotations.Periodic;

public class PeriodicScheduler {

	private ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(2);
	private LinkedHashMap<Integer, Vector<ScheduledFuture<?>>> callbacks = new LinkedHashMap<Integer, Vector<ScheduledFuture<?>>>();
	private final Bus bus;
	
	public PeriodicScheduler(Bus bus) {
		this.bus = bus;
		bus.register(new PeriodicDispatcher());
	}	
	
	public void stopAll() {
		exec.shutdown();
	}

	public void unregister(Object pojo) {
		Vector<ScheduledFuture<?>> calls = callbacks.remove(pojo.hashCode());
		if (calls != null)
			for (ScheduledFuture<?> t : calls)
				t.cancel(true);
	}

	public void register(Object pojo) {
		for (Method m : pojo.getClass().getDeclaredMethods()) {
			if (m.getAnnotation(Periodic.class) != null) {

				if (m.getParameterTypes().length != 0) {
					System.err
					.println("Warning: Ignoring @Periodic annotation on method "
							+ m + " due to wrong number of parameters.");
					continue;
				}
				m.setAccessible(true);

				Runnable callback = new Runnable() {

					final PeriodicMethod per = new PeriodicMethod(m, pojo);

					@Override
					public void run() {
						try {
							bus.post(per);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				long period = m.getAnnotation(Periodic.class).value();
				ScheduledFuture<?> c = exec.scheduleAtFixedRate(callback, period, period, TimeUnit.MILLISECONDS);

				if (!callbacks.containsKey(pojo.hashCode()))
					callbacks.put(pojo.hashCode(), new Vector<ScheduledFuture<?>>());
				callbacks.get(pojo.hashCode()).add(c);
			}
		}
	}

}
