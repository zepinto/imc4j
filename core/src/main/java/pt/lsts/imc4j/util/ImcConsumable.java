package pt.lsts.imc4j.util;

import pt.lsts.imc4j.msg.Abort;
import pt.lsts.imc4j.msg.Message;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This interface supplies a class with a simple publish/subscribe mechanism for IMC messages
 */
public interface ImcConsumable {
    ConcurrentHashMap<Class<?>, ArrayList<Consumer<?>>> consumers = new ConcurrentHashMap<>();
    Executor executor = Executors.newCachedThreadPool();

    /**
     * This method is used by consumers to subscribe to a specific message type
     * @param msg The class of messages to subscribe
     * @param consumer A Consumer (method reference) of given message type
     */
    default <M extends Message> void subscribe(Class<M> msg, Consumer<M> consumer) {
        synchronized (consumers) {
            consumers.putIfAbsent(msg, new ArrayList<>());
            consumers.get(msg).add(consumer);
        }
    }

    /**
     * This method is used by the producer to publish messages to the consumers.
     * All consumers receive the messages asynchronously (using an Async Executor)
     * @param m The message to publish
     */
    @SuppressWarnings("unchecked")
    default <M extends Message> void publish(M m) {
        consumers.getOrDefault(m.getClass(), new ArrayList<>()).forEach(c -> executor.execute(() -> ((Consumer<M>)c).accept(m)));
    }
}