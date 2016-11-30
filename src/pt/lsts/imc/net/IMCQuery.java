package pt.lsts.imc.net;

import pt.lsts.imc.msg.EstimatedState;
import pt.lsts.imc.msg.Message;

/**
 * Created by zp on 30-11-2016.
 */
public class IMCQuery<T> {

    private String source = null;
    private String entity = null;
    private String msgName;
    private IMCQuery() {

    }

    public static IMCQuery<Message> q(String abbrev) {
        IMCQuery<Message> q = new IMCQuery<>();
        q.msgName = abbrev;
        return q;
    }

    public static <T extends Message> IMCQuery<T> q(Class<T> clazz) {
        IMCQuery<T> q = new IMCQuery<>();
        q.msgName = clazz.getSimpleName();
        return q;
    }

    public IMCQuery<T> src(String source) {
        this.source = source;
        return this;
    }

    public IMCQuery<T> ent(String entity) {
        this.entity = entity;
        return this;
    }

    @SuppressWarnings("unchecked")
	public T now() {
    	if (source == null && entity == null) {
    		return (T) IMCNetwork.state().ofType(msgName);
    	}        	
    	else if (source != null && entity == null) {
            return (T) IMCNetwork.state().get(source, msgName);
    	}
        else {
        	return (T) IMCNetwork.state().get(source, msgName, entity);
        }
    }

    public T poll(long millis) {
        //TODO
    	return null;
    }


    public static void main(String[] args) {
        IMCQuery.q(EstimatedState.class).src("lauv-xplore-1").ent("Navigation").now();
    }

}
