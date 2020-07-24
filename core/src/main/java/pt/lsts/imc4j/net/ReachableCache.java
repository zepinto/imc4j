package pt.lsts.imc4j.net;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * This class is used to check if a remote peer is reachable
 */
public class ReachableCache {

    static LinkedHashMap<String, HostReachability> reachabilityCache = new LinkedHashMap<>();
    static ExecutorService executorService = Executors.newCachedThreadPool();

    public static Future<Boolean> isReachable(String hostname) {
        return executorService.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isReachableBlocking(hostname);
            }
        });
    }

    public static boolean isReachableBlocking(String hostname) {

        synchronized (reachabilityCache) {
            HostReachability reachability = reachabilityCache.get(hostname);
            if (reachability != null && reachability.getAge() < 60000) {
                return reachability.isReachable();
            }
            else {
                try {
                    if (InetAddress.getByName(hostname).isReachable(2500))
                        reachabilityCache.put(hostname, new HostReachability(true));
                    else
                        reachabilityCache.put(hostname, new HostReachability(false));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    reachabilityCache.put(hostname, new HostReachability(false));
                }

            }
            return reachabilityCache.get(hostname).isReachable();
        }
    }

    public static String firstReachable(long timeout, String... hostnames) {
        long endTime = System.currentTimeMillis() + timeout;

        LinkedHashMap<String, Future<Boolean>> pings = new LinkedHashMap<>();

        for (String host : hostnames)
            pings.put(host, isReachable(host));

        while(System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(25);
                for (Map.Entry<String, Future<Boolean>> ping : pings.entrySet()) {
                    try {
                        if (ping.getValue().isDone() && ping.getValue().get())
                            return ping.getKey();
                    }
                    catch (Exception e) {
                        // nothing
                    }
                }
            }
            catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private static class HostReachability {
        long lastTested;
        boolean reachable;

        public HostReachability(boolean reachable) {
            this.reachable = reachable;
            this.lastTested = System.currentTimeMillis();
        }

        long getAge() {
            return System.currentTimeMillis() - lastTested;
        }

        boolean isReachable() {
            return reachable;
        }
    }
}
