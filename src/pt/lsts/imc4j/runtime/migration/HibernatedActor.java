package pt.lsts.imc4j.runtime.migration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import pt.lsts.imc4j.runtime.IMCRuntime;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;
import pt.lsts.imc4j.test.ActorTest;
import pt.lsts.imc4j.util.CompressionUtils;
import pt.lsts.imc4j.util.PojoConfig;

public class HibernatedActor implements Serializable {

	private static final long serialVersionUID = -7325155827260598933L;
	public Properties params = new Properties();
	public String className = null;
	public byte[] classSpec = null;

	public AbstractActor wakeUp(ActorContext context) throws Exception {
		return new MigrationClassLoader(this).get(context);
	}
	
	public HibernatedActor(AbstractActor actor) {
		try {
			params = PojoConfig.getProperties(actor);
			className = actor.getClass().getName();
			String file = className.replaceAll("\\.", "" + File.separatorChar) + ".class";
			InputStream is = getClass().getClassLoader().getResourceAsStream(file);

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			classSpec = CompressionUtils.compress(buffer.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		ActorContext context = new IMCRuntime();
		ActorTest at = new ActorTest(context);
		context.start();
				
		Thread.sleep(15000);
		
		System.out.println("now doing hibernation...");
		context.unregister(at);
		HibernatedActor h = new HibernatedActor(at);
		
		Thread.sleep(15000);
		System.out.println("now reviving...");
		
		h.wakeUp(context);
	}

}
