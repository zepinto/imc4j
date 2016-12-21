package pt.lsts.imc.actors;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.annotations.Publish;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.net.IMCNetwork;
import pt.lsts.imc.util.PojoConfig;

/**
 * Created by zp on 30-11-2016.
 */
public abstract class IMCActor {

    private static final LinkedHashMap<IMCActor, Integer> entities = new LinkedHashMap<>();
    private static final LinkedHashMap<IMCActor, ArrayList<Class<?>>> inputs = new LinkedHashMap<>();
    private static final LinkedHashMap<IMCActor, ArrayList<Class<?>>> outputs = new LinkedHashMap<>();
    private final LinkedHashMap<Class<?>, Boolean> checks = new LinkedHashMap<>();

    public final int id() {
        synchronized (entities) {
            if (!entities.containsKey(this)) {
                entities.put(this, entities.size()+1);
            }
            return entities.get(this);
        }
    }

    public String name() {
    	return getClass().getSimpleName();        
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
        IMCNetwork.post(msg);
    }

    public final boolean send(String destination, Message msg) throws Exception {
        check(msg);
        try {
            IMCNetwork.sendUdp(msg, destination);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
    
    public void init() {
    	
    }
    
    public void finish() {
    	
    }
    
    public void run() {
    	run(new Properties());
    }
    
    public void run(Properties p) {
    	
    	try {
    		PojoConfig.setProperties(this, p);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	init();
    	
    	try {
            IMCNetwork.start();
            IMCNetwork.register(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    	finish();
    }
}
