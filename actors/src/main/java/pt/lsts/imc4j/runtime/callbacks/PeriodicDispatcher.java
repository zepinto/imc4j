package pt.lsts.imc4j.runtime.callbacks;

import com.squareup.otto.Subscribe;

public class PeriodicDispatcher {
	@Subscribe
	public void on(PeriodicMethod callback) {
		callback.invoke();
	}	
}
