package pt.lsts.imc4j.net;

import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is used to check if a remote peer is reachable
 * It caches results for 60 seconds
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
