package pt.lsts.imc.net;

import java.util.LinkedHashMap;

import com.squareup.otto.Subscribe;

import pt.lsts.imc.msg.Announce;
import pt.lsts.imc.msg.EntityList;
import pt.lsts.imc.msg.EntityList.OP;

/**
 * This module will request the entity list to all nodes that connect
 * 
 * @author zp
 *
 */
public class EntityListRequester {
	private LinkedHashMap<String, Long> requested = new LinkedHashMap<>();

	@Subscribe
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
				IMCNetwork.sendUdp(req, announce.sys_name);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
