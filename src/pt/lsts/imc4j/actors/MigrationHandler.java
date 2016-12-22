package pt.lsts.imc4j.actors;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.NeptusBlob;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;
import pt.lsts.imc4j.runtime.migration.HibernatedActor;

public class MigrationHandler extends AbstractActor {

	private ActorContext context;
	
	public MigrationHandler(ActorContext context) {
		super(context);
		this.context = context;
	}

	@Subscribe
	public void on(NeptusBlob blob) {
		if (blob.content_type.startsWith("migration/imc4j")) {
			try {
				HibernatedActor ha = HibernatedActor.fromBlob(blob);
				ha.wakeUp(context);				
				NeptusBlob copy = (NeptusBlob) Message.deserialize(blob.serialize());
				copy.content_type = "migration/succeeded";
				reply(blob, copy);	
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					NeptusBlob copy = (NeptusBlob) Message.deserialize(blob.serialize());
					copy.content_type = "migration/failed";
					reply(blob, copy);	
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}				
			}
		}
	}
}
