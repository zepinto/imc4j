package pt.lsts.autonomy.soi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Properties;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.PlanControl;
import pt.lsts.imc4j.msg.PlanControl.OP;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.msg.PlanSpecification;

public class SoiExecutive extends TimedFSM {

	private SoiPlan plan = new SoiPlan();

	@Parameter(description = "Nominal Speed")
	double speed = 1;

	@Parameter(description = "Maximum Speed")
	double max_speed = 1.5;

	@Parameter(description = "Minimum Speed")
	double min_speed = 0.7;

	@Parameter(description = "Maximum Depth")
	double max_depth = 10;

	@Parameter(description = "Minimum Depth")
	double min_depth = 0.0;

	@Parameter(description = "DUNE Host Address")
	String host_addr = "127.0.0.1";

	@Parameter(description = "DUNE Host Port (TCP)")
	int host_port = 6003;

	@Parameter(description = "Minutes before termination")
	int mins_timeout = 60;

	public SoiExecutive() {
		state = this::init;
	}

	@Consume
	public void on(PlanControl pc) {
		if (pc.op == OP.PC_LOAD) {
			plan = SoiPlan.parse((PlanSpecification) pc.arg);
			state = this::loadPlan;
		}
	}

	public FSMState init(FollowRefState state) {
		deadline = new Date(System.currentTimeMillis() + mins_timeout * 60 * 1000);
		return this::idle;
	}
	
	public FSMState idle(FollowRefState state) {
		return this::idle;
	}

	public FSMState loadPlan(FollowRefState state) {
		return this::exec;
	}

	public FSMState exec(FollowRefState state) {
		return this::exec;
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage: java -jar SoiExec.jar <FILE>");
			System.exit(1);
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			SoiExecutive tmp = new SoiExecutive();
			writer.write("#SOI Executive settings\n\n");
			for (Field f : tmp.getClass().getDeclaredFields()) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					writer.write("#" + p.description() + "\n");
					writer.write(f.getName() + "=" + f.get(tmp) + "\n\n");
				}
			}
			System.out.println("Wrote default properties to " + file.getAbsolutePath());
			writer.close();
			System.exit(0);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(file));

		SoiExecutive tracker = PojoConfig.create(SoiExecutive.class, props);

		System.out.println("Executive started with settings:");
		for (Field f : tracker.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				System.out.println(f.getName() + "=" + f.get(tracker));
			}
		}
		System.out.println();

		tracker.connect(tracker.host_addr, tracker.host_port);
		tracker.join();
	}

}
