package pt.lsts.imc4j.runtime.clock;

public interface ActorClock {

	/**
	 * Synchronously hold execution for some time
	 * @param millis Amount of time to wait, in milliseconds
	 * @throws InterruptedException In case the Thread is interrupted while waiting 
	 */
	public void sleep(long millis) throws InterruptedException;
	
	/**
	 * Retrieve the epoch time in milliseconds
	 * @return Amount of milliseconds since 1st January 1970
	 */
	public long curTime();
	
	/**
	 * Convert simulated duration into real-time duration
	 * @param millis Simulated milliseconds duration
	 * @return The real-time duration corresponding to given duration
	 */
	public long duration(long millis);
}
