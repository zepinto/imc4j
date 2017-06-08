package pt.lsts.backseat.ferry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.util.PojoConfig;

public class DataFerry extends TimedFSM {

	@Parameter(description = "Host for the controlled vehicle")
	String host_addr = "127.0.0.1";

	@Parameter(description = "Port for the controlled vehicle")
	int host_port = 6002;

	@Parameter(description = "Port for the controlled vehicle")
	int timeout_mins = 60;

	@Parameter(description = "First depot position")
	String pos1 = "0,0,5,10";

	@Parameter(description = "First depot position")
	String pos2 = "-200,0,20,10";

	@Parameter(description = "First depot position")
	String pos3 = "0,0,35,10";

	@Parameter(description = "First depot position")
	String pos4 = "-200,0,50,10";

	@Parameter(description = "First depot position")
	String pos5 = "";

	@Parameter(description = "First depot position")
	String pos6 = "";

	@Parameter(description = "First depot position")
	String pos7 = "";

	private ArrayList<double[]> schedule = new ArrayList<>();

	void init() {
		deadline = new Date(System.currentTimeMillis() + timeout_mins * 60 * 1000);
		try {
			Properties props = PojoConfig.getProperties(this);
			for (int i = 1; i < 8; i++) {
				String val = props.getProperty("pos" + i);
				if (val.isEmpty())
					break;
				String[] parts = val.split("[, ]");
				double[] point = new double[] { Double.valueOf(parts[0]), Double.valueOf(parts[1]),
						Double.valueOf(parts[2]), Double.valueOf(parts[3]) };
				schedule.add(point);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DataFerry() {
		state = this::exec;
	}

	FSMState exec(FollowRefState state) {
		return this::exec;
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage: java -jar Ferry.jar <FILE>");
			System.exit(1);
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			DataFerry tmp = new DataFerry();
			writer.write("#Data Ferry Settings\n\n");
			for (Field f : tmp.getClass().getDeclaredFields()) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					writer.write("#" + p.description() + "\n");
					writer.write(f.getName() + "=" + f.get(tmp) + "\n\n");
				}
			}
			System.out.println("Wrote default properties to " + file.getName());
			writer.close();
			System.exit(0);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(file));

		DataFerry ferry = PojoConfig.create(DataFerry.class, props);
		ferry.init();

		System.out.println("Data Ferry started with settings:");
		for (Field f : ferry.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				System.out.println(f.getName() + "=" + f.get(ferry));
			}
		}
		System.out.println();

		ferry.connect(ferry.host_addr, ferry.host_port);
		ferry.join();
	}

}
