package pt.lsts.imc4j.net;

import java.util.LinkedHashMap;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EntityList;
import pt.lsts.imc4j.msg.EntityList.OP;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;

/**
 * This module will request the entity list to all nodes that connect
 * 
 * @author zp
 *
 */
public class EntityListRequester extends AbstractActor {
	
	public EntityListRequester(ActorContext context) {
		super(context);
	}

	private LinkedHashMap<String, Long> requested = new LinkedHashMap<>();

	@Subscribe
	@Publish(EntityList.class)
	public void on(Announce announce) {
		Long lastRequest;
		synchronized (requested) {
			lastRequest = requested.get(announce.sys_name);
		}
		if (lastRequest == null || System.currentTimeMillis() - lastRequest > 60000) {
			try {
				synchronized (requested) {
					lastRequest = System.currentTimeMillis();
				}
				EntityList req = new EntityList();
				req.op = OP.OP_QUERY;
				send(announce.sys_name, req);
				
			} catch (Exception e) {

			}
		}
	}
}
