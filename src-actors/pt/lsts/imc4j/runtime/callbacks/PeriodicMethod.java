package pt.lsts.imc4j.runtime.callbacks;

import java.lang.reflect.Method;

public class PeriodicMethod {

	private final Method method;
	private final Object actor;
	
	public PeriodicMethod(Method method, Object actor) {
		this.method = method;		
		this.actor = actor;
	}
	
	public void invoke() {
		method.setAccessible(true);
		try {
			method.invoke(actor);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
