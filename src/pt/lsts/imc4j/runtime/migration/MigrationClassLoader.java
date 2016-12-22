package pt.lsts.imc4j.runtime.migration;

import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;
import pt.lsts.imc4j.util.CompressionUtils;

public class MigrationClassLoader extends ClassLoader {

	private HibernatedActor actor;
	
	public MigrationClassLoader(HibernatedActor actor) {
		this.actor = actor;
	}
	
	public AbstractActor get(ActorContext context) throws Exception {
		byte[] data = CompressionUtils.decompress(actor.classSpec);
		Class<?> c = defineClass(actor.className, data, 0, data.length);
		AbstractActor act = (AbstractActor) c.getConstructor(ActorContext.class).newInstance(context);
		act.init(actor.params);
		return act;
	}
	
}
