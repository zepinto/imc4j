package pt.lsts.imc4j.runtime.migration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Properties;

import pt.lsts.imc4j.msg.NeptusBlob;
import pt.lsts.imc4j.runtime.IMCRuntime;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;
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
	
	public NeptusBlob asBlob() throws Exception {
		NeptusBlob blob = new NeptusBlob();
		blob.content_type = "migration/imc4j";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this);
		oos.close();
		blob.content = baos.toByteArray();
		return blob;
	}
	
	public static HibernatedActor fromBlob(NeptusBlob blob) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(blob.content));
		return (HibernatedActor) ois.readObject();
	}
	
	private void loadSpec() {
		try {
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
	
	public <T extends AbstractActor> HibernatedActor(Class<T> actor) {
		className = actor.getName();
		loadSpec();
	}
	
	
	
	public HibernatedActor(AbstractActor actor) {
		try {
			params = PojoConfig.getProperties(actor);	
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
		className = actor.getClass().getName();
		loadSpec();
	}
}
