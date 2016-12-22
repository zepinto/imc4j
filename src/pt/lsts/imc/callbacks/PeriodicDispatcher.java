package pt.lsts.imc.callbacks;

import com.squareup.otto.Subscribe;

public class PeriodicDispatcher {
	@Subscribe
	public void on(PeriodicMethod callback) {
		callback.invoke();
	}	
}
