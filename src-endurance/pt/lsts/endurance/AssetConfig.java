package pt.lsts.endurance;

import pt.lsts.imc4j.annotations.Parameter;

public class AssetConfig {

	@Parameter(description = "Nominal Speed")
	double speed = 1;

	@Parameter(description = "Maximum Depth")
	double max_depth = 10;

	@Parameter(description = "Minimum Depth")
	double min_depth = 0.0;

	@Parameter(description = "Maximum Speed")
	double max_speed = 1.5;

	@Parameter(description = "Minimum Speed")
	double min_speed = 0.7;

	@Parameter(description = "DUNE Host Address")
	String host_addr = "127.0.0.1";

	@Parameter(description = "DUNE Host Port (TCP)")
	int host_port = 6006;

	@Parameter(description = "Minutes before termination")
	int mins_timeout = 600;

	@Parameter(description = "Maximum time underwater")
	int mins_under = 10;

	@Parameter(description = "Number where to send reports")
	String sms_number = "+351914785889";

	@Parameter(description = "Seconds to idle at each vertex")
	int wait_secs = 60;

	@Parameter(description = "SOI plan identifier")
	String soi_plan_id = "soi_plan";

	@Parameter(description = "Cyclic execution")
	boolean cycle = false;

}
