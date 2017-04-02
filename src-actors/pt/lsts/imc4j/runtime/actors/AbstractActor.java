package pt.lsts.imc4j.runtime.actors;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.runtime.IMCRuntime;
import pt.lsts.imc4j.util.PojoConfig;

/**
 * Created by zp on 30-11-2016.
 */
public abstract class AbstractActor {

    private static final LinkedHashMap<AbstractActor, ArrayList<Class<?>>> inputs = new LinkedHashMap<>();
    private static final LinkedHashMap<AbstractActor, ArrayList<Class<?>>> outputs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<?>, Boolean> checks = new LinkedHashMap<>();
    private final ActorContext context;
    private int id;
    
    public AbstractActor(ActorContext context) {
    	this.context = context;
    	this.id = context.register(this, entityName());
    }
    
    public Properties params() {
    	try {
    		return PojoConfig.getProperties(this);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
		}
    	return new Properties();
    }
    
    public final int entityId() {
        return id;
    }

    public String entityName() {
    	return getClass().getSimpleName();        
    }
    
    public final int systemId() {
    	return context.registry().getImcId();
    }
    
    public final String systemName() {
    	return context.registry().getSysName();
    }
    
    public String systemName(int imcId) {
    	return context.registry().resolveSystem(imcId);
    }

    public Integer systemId(String sysName) {
    	return context.registry().resolveSystem(sysName);
    }
    
    public Integer entityId(String sysName, String entityName) {
    	return context.registry().resolveSystem(sysName);
    }    
    
    public List<String> peers() {
    	return context.peers();
    }
    
    public void sleep(long millis) throws InterruptedException {
    	context.clock().sleep(millis);
    }
    
    public void duration(long millis) {
    	context.clock().duration(millis);
    }
    
    public long curTime() {
    	return context.clock().curTime();
    }
    
    public final List<Class<?>> outputs() {
        synchronized (outputs) {
            if (outputs.containsKey(this)) {
                return outputs.get(this);
            }
        }
        HashSet<Class<?>> outgoing = new HashSet<>();
        
        for (Method m : getClass().getMethods()) {
            if ((m.getModifiers() & Modifier.PUBLIC) == 0)
                continue;
            if (m.getAnnotation(Publish.class) == null)
                continue;
            for (Class<?> c : m.getAnnotation(Publish.class).value())
            	outgoing.add(c);            
        }
        
        ArrayList<Class<?>> ret = new ArrayList<>(outgoing);
        
        synchronized (outputs) {
        	outputs.put(this, ret);
        }
        return ret;
    }

    public final List<Class<?>> inputs() {
        synchronized (inputs) {
            if (inputs.containsKey(this)) {
                return inputs.get(this);
            }
        }
        ArrayList<Class<?>> subscriptions = new ArrayList<>();

        for (Method m : getClass().getMethods()) {
            if ((m.getModifiers() & Modifier.PUBLIC) == 0)
                continue;
            if (m.getParameterTypes().length != 1)
                continue;
            if (m.getAnnotation(Subscribe.class) == null)
                continue;
            subscriptions.add(m.getParameterTypes()[0]);
        }

        synchronized (inputs) {
            inputs.put(this, subscriptions);
        }

        return subscriptions;
    }

    private void check(Message msg) throws Exception {
        Boolean admissible;
        synchronized (checks) {
            admissible = checks.get(msg.getClass());
        }

        if (admissible == null) {
            admissible = false;
            for (Class<?> c : outputs()) {
                if (c.isAssignableFrom(msg.getClass())) {
                    admissible = true;
                    break;
                }
            }
            synchronized (checks) {
                checks.put(msg.getClass(), admissible);
            }
        }

        if (!admissible) {
            throw new Exception("Message cannot be sent from this actor");
        }
    }

    public final void post(Message msg) throws Exception {
        check(msg);
        context.post(msg);
    }

    public final void reply(Message request, Message reply) throws Exception {
        check(reply);
        context.reply(request, reply);        
    }
    
    
    public final int send(Message msg) throws Exception {
        check(msg);
        return context.send(msg);        
    }
    
    public final boolean send(String destination, Message msg) throws Exception {
        check(msg);
        try {
            context.send(msg, destination);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public void init() {
    	
    }
    
    public void init(Properties p) {
    	try {
    		PojoConfig.setProperties(this, p);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	init();
    }
    
    public void finish() {
    	
    }
    
    public static  void exec(Class<?>... actors) throws Exception {
    	exec(new Properties(), actors);
    	
    }
    public static void exec(Properties props, Class<?>... actors) throws Exception {
    	IMCRuntime context = new IMCRuntime();
    	for (Class<?> c : actors) {
    		AbstractActor actor = (AbstractActor) c.getConstructor(ActorContext.class).newInstance(context);
    		actor.init(props);
    		context.start();
    	}
    }
}
