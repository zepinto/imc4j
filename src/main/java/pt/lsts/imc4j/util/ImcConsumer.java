package pt.lsts.imc4j.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.msg.Message;

public class ImcConsumer {

	private LinkedHashMap<Class<?>, ArrayList<Method>> consumeMethods = new LinkedHashMap<Class<?>, ArrayList<Method>>();
	private Object pojo;

	private ImcConsumer(Object pojo) {
		this.pojo = pojo;

		Class<?> clazz = pojo.getClass();
		while (clazz != Object.class) {
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.getAnnotation(Consume.class) != null) {
					if (m.getParameterTypes().length != 1) {
						System.err.println("Warning: Ignoring @Consume annotation on method " + m
								+ " due to wrong number of parameters.");
						continue;
					}
					if (!Message.class.isAssignableFrom(m.getParameterTypes()[0])) {
						System.err.println("Warning: Ignoring @Consume annotation on method " + m
								+ " due to wrong parameter type.");
						continue;
					}

					Class<?> c = m.getParameterTypes()[0];

					if (!consumeMethods.containsKey(c)) {
						consumeMethods.put(c, new ArrayList<Method>());
					}
					if (!m.isAccessible())
						m.setAccessible(true);

					consumeMethods.get(c).add(m);
				}
			}

			clazz = clazz.getSuperclass();
		}
	}

	public void onMessage(Message m) {
		Class<?> c = m.getClass();
		ArrayList<Method> consumers = new ArrayList<Method>();

		while (c != Object.class) {
			if (consumeMethods.containsKey(c))
				consumers.addAll(consumeMethods.get(c));
			c = c.getSuperclass();
		}

		for (Method method : consumers) {
			try {
				method.invoke(pojo, m);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static ImcConsumer create(final Object pojo) {
		return new ImcConsumer(pojo);
	}

	/**
	 * @return the pojo
	 */
	public final Object getPojo() {
		return pojo;
	}
}
