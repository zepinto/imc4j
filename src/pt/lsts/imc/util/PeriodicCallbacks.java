package pt.lsts.imc.util;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pt.lsts.imc.annotations.Periodic;

public class PeriodicCallbacks {
	private static ScheduledThreadPoolExecutor _exec = null;
	
	private static ScheduledThreadPoolExecutor executor() {
		if (_exec == null)
			_exec = new ScheduledThreadPoolExecutor(2);
		return _exec;
	}
	
	private static LinkedHashMap<Integer, Vector<ScheduledFuture<?>>> callbacks = new LinkedHashMap<Integer, Vector<ScheduledFuture<?>>>();
	
	public static void stopAll() {
		executor().shutdown();
		_exec = null;
	}
	
	public static void unregister(Object pojo) {
		Vector<ScheduledFuture<?>> calls = callbacks.remove(pojo.hashCode());
		if (calls != null)
			for (ScheduledFuture<?> t : calls)
				t.cancel(true);
	}
	
	public static void register(Object pojo) {
		for (Method m : pojo.getClass().getDeclaredMethods()) {
			if (m.getAnnotation(Periodic.class) != null) {

				if (m.getParameterTypes().length != 0) {
					System.err
							.println("Warning: Ignoring @Periodic annotation on method "
									+ m + " due to wrong number of parameters.");
					continue;
				}
				m.setAccessible(true);
				final Method method = m;
				final Object client = pojo;
				
				Runnable callback = new Runnable() {

					@Override
					public void run() {
						try {
							method.invoke(client);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};

				long period = method.getAnnotation(Periodic.class)
						.value();
				
				ScheduledFuture<?> c = executor().scheduleAtFixedRate(callback, period, period, TimeUnit.MILLISECONDS);
				
				if (!callbacks.containsKey(pojo.hashCode()))
					callbacks.put(pojo.hashCode(), new Vector<ScheduledFuture<?>>());
				callbacks.get(pojo.hashCode()).add(c);
			}
		}
	}
}
