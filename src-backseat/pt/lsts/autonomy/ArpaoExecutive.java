package pt.lsts.autonomy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;

import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.CompassCalibration;
import pt.lsts.imc4j.msg.CompassCalibration.DIRECTION;
import pt.lsts.imc4j.msg.EntityParameter;
import pt.lsts.imc4j.msg.EntityParameters;
import pt.lsts.imc4j.msg.Goto;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PlanControlState;
import pt.lsts.imc4j.msg.PlanControlState.LAST_OUTCOME;
import pt.lsts.imc4j.msg.PlanDB;
import pt.lsts.imc4j.msg.PlanDBState;
import pt.lsts.imc4j.msg.PlanDB.OP;
import pt.lsts.imc4j.msg.PlanDB.TYPE;
import pt.lsts.imc4j.msg.PlanDBInformation;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.PopUp;
import pt.lsts.imc4j.msg.PopUp.FLAGS;
import pt.lsts.imc4j.msg.QueryEntityParameters;
import pt.lsts.imc4j.msg.Sms;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

public class ArpaoExecutive extends MissionExecutive {

	@Parameter(description = "Sequence of plans to execute after the vehicle is ready")
	public String[] plans = new String[] { };

    @Parameter(description = "DUNE plan to execute right after termination (empty for not use)")
    private String endPlanToUse = "";

	@Parameter(description = "DUNE hostname")
	public String host = "127.0.0.1";

	@Parameter(description = "DUNE TCP port")
	public int port = 6003;

	@Parameter(description = "If set to true, the vehicle will calibrate the compass prior to mission execution")
	public boolean calibrate_compass = true;
	
	@Parameter(description = "Time, in minutes, to spend calibrating the compass")
	public int compass_calib_mins = 15;
	
	@Parameter(description = "Number of tries to calibrate the compass (min=1)")
    public int compass_calib_tries = 2;
	
	@Parameter(description = "If set ot true, the vehicle will align IMU prior to mission execution")
	public boolean align_imu = true;
	
	@Parameter(description = "Length of IMU alignment track")
	public double imu_align_length = 250;

	@Parameter(description = "Length of transit before IMU alignment track")
	public double imu_transit_before_align_length = 0;

	@Parameter(description = "Bearing of IMU alignment track")
	public double imu_align_bearing = 250;

    @Parameter(description = "Number of tries to align IMU (min=1)")
    public int imu_align_tries = 2;

    @Parameter(description = "Send status over SMS")
    public boolean sms_updates = false;
    
    @Parameter(description = "GSM Number where to send reports. Leave empty to use the emergency number.")
    public String gsm_number = "";
	
    protected int requestIdConter = 0;
    protected long time = 0;
    protected String plan = null;
    protected int plan_index = 0;
	private String emergencyNumber = null;
	private String alignManeuverId = "";
	private int compassCalibrationTriesCounter = 0;
	private int imuAlignTriesCounter = 0;
	
	private PlanDB planDBListMsg = null;
	
	private class Offset {
	    public double x;
	    public double y;
	    
	    public Offset(double x, double y) {
	        this.x = x;
	        this.y = y;
        }
	}
	
	public ArpaoExecutive() {
		state = this::init;
	}

	public void setupChild() {
        if (!endPlanToUse.isEmpty()) {
            endPlan = endPlanToUse;
        }
        
        compassCalibrationTriesCounter = Math.max(1, compass_calib_tries);
        imuAlignTriesCounter = Math.max(1, imu_align_tries);
	}
	
	@Override
	protected void launchChild() {
	    init();
	}
	
	@Override
	public void connect() throws Exception {
	    connect(host, port);
	}

	public State init() {
		if (sms_updates) {
		    if (!knowsEmergencyNumber()) {
		        QueryEntityParameters query = new QueryEntityParameters();
		        query.name = "Emergency Monitor";
		        query.scope = "global";
		        query.visibility = "user";
		        try {
		            send(query);	
		        }
		        catch (Exception e) {
		            e.printStackTrace();
		        }
		        print("Waiting to get GSM emergency number...");
		        return this::init;
		    }
		}
		else {
		    print("SMS updates disabled...");
		}
		
		if (!atSurface()) {
			print("Waiting for the vehicle to be at surface...");
			return this::init;
		}

		if (!hasGps()) {
			print("Waiting for GPS...");
			return this::init;
		}
		
		if (plans == null || plans.length == 0) {
		    print("Plan list is empty...");
		    sendMessage("Plan list is empty... Exiting.");
		    prepExitNoEndPlan();
		    systemExit(-240);
            return null;
		}
		else {
		    if (planDBListMsg == null) {
		        PlanDB query = new PlanDB();
		        query.type = PlanDB.TYPE.DBT_REQUEST;
		        query.op = PlanDB.OP.DBOP_GET_STATE;
		        query.request_id = requestIdConter++;;
		        try {
		            send(query);    
		        }
		        catch (Exception e) {
		            e.printStackTrace();
		        }
		        print("Waiting to get PlanDB plan list...");
	            return this::init;
		    }
		    else {
		        if (!isPlanListAndEndPlanOnVehicle()) {
		            print("Plan list (including end plan) not on the vehicle...");
		            sendMessage("Plan list (including end plan) not on the vehicle... Exiting.");
		            prepExitNoEndPlan();
		            systemExit(-241);
		            return null;
		        }
		    }
		}
		
		if (!ready()) {
			print("Waiting for the vehicle to be ready...");
			return this::init;
		}
		
		if (calibrate_compass) {
			PlanSpecification spec = ccalib();
			plan = spec.plan_id;
			time = System.currentTimeMillis();
			sendMessage("Starting compass calibration ("+compass_calib_mins+" mins).");
			exec(spec);
			compassCalibrationTriesCounter--;
			return this::compass_calib;
		} 
		else if (align_imu) {
			PlanSpecification spec = imu();
			plan = spec.plan_id;
			time = System.currentTimeMillis();
			sendMessage("Starting IMU alignment.");
			exec(spec);
	        imuAlignTriesCounter--;
			return this::imu_align;
		} 
		else {
			plan = "";
			time = System.currentTimeMillis();
			print("Starting plan sequence execution.");
			return this::plan_exec;
		}
	}
	
	private boolean isPlanListAndEndPlanOnVehicle() {
	    if (planDBListMsg == null)
	        return false;
	    
	    PlanDB pdb = planDBListMsg;
	    if (pdb.arg != null && pdb.arg instanceof PlanDBState) {
	        PlanDBState planDBState = (PlanDBState) pdb.arg;
	        ArrayList<PlanDBInformation> planInfoMsgList = planDBState.plans_info;
	        if (planInfoMsgList.isEmpty()) {
	            return false;
	        }
	        int planExistCounter = 0;
	        for (PlanDBInformation pdbi : planInfoMsgList) {
                for (String pl : plans) {
                    if (pdbi.plan_id.trim().equals(pl.trim())) {
                        planExistCounter++;
                    }
                }
	            if (endPlanToUse != null && !endPlanToUse.isEmpty()) {
	                if (pdbi.plan_id.trim().equals(endPlanToUse.trim())) {
                        planExistCounter++;
                    }
	            }
            }
	        int planNumberToCheck = plans.length + (endPlanToUse != null && !endPlanToUse.isEmpty() ? 1 : 0);
	        if (planNumberToCheck == planExistCounter)
	            return true;
	        else
	            return false;
	    }
	    
	    return false;
    }

    private void prepExitNoEndPlan() {
        endPlan = "";
    }

    protected void sendMessage(String message) {
		try {
			if (sms_updates) {
			    Sms sms = new Sms();
			    sms.contents = message;
			    sms.number = emergencyNumber;
			    sms.timeout = 60;
				print("Sending SMS to '"+emergencyNumber+"'with contents: '"+message+"'.");
				send(sms);	
			}
			else {
				print(message+" - Not sending SMS -");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Consume
	protected void on(EntityParameters params) {
		for (EntityParameter p : params.params) {
			if (p.name.equals("SMS Recipient Number"))
				emergencyNumber = p.value;
		}
	}

	@Consume
    protected void on(PlanDB msg) {
	    if (msg.type == TYPE.DBT_SUCCESS) {
	        if (msg.op == OP.DBOP_GET_STATE) {
	            planDBListMsg = msg;
	        }
	    }
	}
	
	public State compass_calib() {
		print("compass_calib");

		if (System.currentTimeMillis() - time < 5000)
			return this::compass_calib;

		PlanControlState pcs = get(PlanControlState.class);

		if (pcs != null && pcs.plan_id.equals(plan) && ready()) {
			if (pcs.last_outcome == LAST_OUTCOME.LPO_SUCCESS) {
				if (align_imu) {
					PlanSpecification spec = imu();
					plan = spec.plan_id;
					time = System.currentTimeMillis();
					sendMessage("Starting IMU alignment.");
					exec(spec);
					imuAlignTriesCounter--;
					return this::imu_align;
				}
				else {
					plan = "";
					time = System.currentTimeMillis();
					sendMessage("Starting plan sequence execution.");
					return this::plan_exec;
				}
			}
			else if (compassCalibrationTriesCounter > 0) {
				return this::init;
			}
			else {
			    sendMessage("Calibration not successful... terminating...");
			    return null;
			}
		}

		return this::compass_calib;
	}

	public State imu_align() {
		print("imu_align");
		if (System.currentTimeMillis() - time < 5000)
			return this::imu_align;

		// Activate imu on the last goto maneuver
		if (get(PlanControlState.class).man_id.equals(alignManeuverId)) {
			print("Requesting IMU activation");
			activate("IMU");
		}
		
		if (imuIsAligned()) {
			print("Navigation is aligned");
			stopPlan();
			time = System.currentTimeMillis();
			plan = "";
			sendMessage("Starting plan sequence execution.");
			return this::plan_exec;
		}

		if (ready()) {
		    if (imuAlignTriesCounter <= 0) {
		        sendMessage("IMU alignment not successful... terminating...");
		        return null;
		    }
		    
			print("Deactivating IMU");
			deactivate("IMU");
			PlanSpecification spec = imu();
			plan = spec.plan_id;
			time = System.currentTimeMillis();
			sendMessage("Restarting IMU alignment.");
			exec(spec);
			imuAlignTriesCounter--;
			return this::imu_align;
		}

		return this::imu_align;
	}

	public State plan_exec() {
		print("plan_exec");
		if (System.currentTimeMillis() - time < 5000)
			return this::plan_exec;

		if (ready()) {
			PlanControlState pcs = get(PlanControlState.class);
			if (pcs.plan_id.equals(plan)) {
				 if (pcs.last_outcome.equals(LAST_OUTCOME.LPO_SUCCESS)) {
					 plan_index++;
					 sendMessage(plan + " finished successfully.");
				 }
				 else {
					 print(plan + " was not completed. Terminating...");
					 sendMessage(plan + " was not completed. Terminating...");
					 return null;
				 }
			}
				
			if (plan_index >= plans.length) {
				sendMessage("All plans have finished successfully! Terminating.");
				return null;
			}
			else {
				plan = plans[plan_index];
				sendMessage("Starting execution of '" + plan + "'...");
				startPlan(plan);

				time = System.currentTimeMillis();
			}
		}
		return this::plan_exec;
	}

	public boolean knowsEmergencyNumber() {
		if (!gsm_number.isEmpty())
			emergencyNumber = gsm_number;
		
		return emergencyNumber != null;
	}
	
	public PlanSpecification ccalib() {
		double[] pos = position();

		if (pos == null)
			return null;

		PopUp popup = new PopUp();
		popup.lat = Math.toRadians(pos[0]);
		popup.lon = Math.toRadians(pos[1]);
		popup.speed = 1;
		popup.speed_units = SpeedUnits.METERS_PS;
		popup.flags.add(FLAGS.FLG_CURR_POS);
		popup.duration = 30;
		popup.z = 0;
		popup.z_units = ZUnits.DEPTH;

		CompassCalibration ccalib = new CompassCalibration();
		ccalib.lat = Math.toRadians(pos[0]);
		ccalib.lon = Math.toRadians(pos[1]);
		ccalib.speed = 1;
		ccalib.speed_units = SpeedUnits.METERS_PS;
		ccalib.direction = DIRECTION.LD_CLOCKW;
		ccalib.amplitude = 0;
		ccalib.z = 0;
		ccalib.z_units = ZUnits.DEPTH;
		ccalib.duration = compass_calib_mins * 60;
		ccalib.radius = 15;

		return spec(popup, ccalib);
	}

	public PlanSpecification imu() {
		double[] pos = position();

		if (pos == null)
			return null;

		ArrayList<Maneuver> maneuvers = new ArrayList<>();
		
		PopUp popup = new PopUp();
		popup.lat = Math.toRadians(pos[0]);
		popup.lon = Math.toRadians(pos[1]);
		popup.speed = 1;
		popup.speed_units = SpeedUnits.METERS_PS;
		popup.flags.add(FLAGS.FLG_CURR_POS);
		popup.duration = 30;
		popup.z = 0;
		popup.z_units = ZUnits.DEPTH;
		
		maneuvers.add(popup);

		int maxTries = Math.max(1, imu_align_tries);
		
		double offTrans = imuAlignTriesCounter != maxTries ? 0 : Math.max(0, imu_transit_before_align_length);
		double offAlign = Math.max(0, imu_align_length + offTrans);
		double off40 = Math.min(40, offTrans > 0 ? offTrans : offAlign);
		
		double offsetX40m = Math.cos(Math.toRadians(imu_align_bearing)) * off40;
		double offsetY40m = Math.sin(Math.toRadians(imu_align_bearing)) * off40;

        double offsetXTransit = offTrans > 0 ? Math.cos(Math.toRadians(imu_align_bearing)) * offTrans : 0;
        double offsetYTransit = offTrans > 0 ? Math.sin(Math.toRadians(imu_align_bearing)) * offTrans : 0;

	    double offsetXAlign = Math.cos(Math.toRadians(imu_align_bearing)) * offAlign;
	    double offsetYAlign = Math.sin(Math.toRadians(imu_align_bearing)) * offAlign;

	    ArrayList<Offset> locsOffsets = new ArrayList<>();
	    if (offTrans > 0) {
	        locsOffsets.add(new Offset(offsetX40m, offsetY40m));
	        locsOffsets.add(new Offset(offsetXTransit, offsetYTransit));
	        locsOffsets.add(new Offset(offsetXAlign, offsetYAlign));
	        locsOffsets.add(new Offset(offsetXTransit, offsetYTransit));
	    }
	    else {
            locsOffsets.add(new Offset(offsetX40m, offsetY40m));
            locsOffsets.add(new Offset(offsetXAlign, offsetYAlign));
            locsOffsets.add(new Offset(0, 0));
	    }

	    for (Offset offset : locsOffsets) {
            double[] loc1 = offset.x == 0 && offset.y == 0 ? pos
                    : WGS84Utilities.WGS84displace(pos[0], pos[1], 0, offset.x, offset.y, 0);
	        Goto man1 = new Goto();
	        man1.lat = Math.toRadians(loc1[0]);
	        man1.lon = Math.toRadians(loc1[1]);
	        man1.speed = 1;
	        man1.speed_units = SpeedUnits.METERS_PS;
	        man1.z = 0;
	        man1.z_units = ZUnits.DEPTH;
	        
	        maneuvers.add(man1);
        }

		PlanSpecification spec = spec(maneuvers.toArray(new Maneuver[maneuvers.size()]));
		alignManeuverId = spec.maneuvers.get(2 + (offTrans > 0 ? 1 : 0)).maneuver_id;
		return spec;
	}
		
	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			System.err.println("Usage: java -jar ArpaoExec.jar <FILE>");
			System.exit(1);
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			ArpaoExecutive tmp = new ArpaoExecutive();
			writer.write("#Arpao Executive Settings\n\n");
			for (Field f : tmp.getClass().getDeclaredFields()) {
				Parameter p = f.getAnnotation(Parameter.class);
				if (p != null) {
					Object value = f.get(tmp);
					if (value instanceof String[]) {
						value = String.join(", ", ((String[]) value));
					}
					writer.write("#" + p.description() + "\n");
					writer.write(f.getName() + "=" + value + "\n\n");
				}
			}
			System.out.println("Wrote default properties to " + file.getAbsolutePath());
			writer.close();
			System.exit(0);
		}

		Properties props = new Properties();
		props.load(new FileInputStream(file));

		ArpaoExecutive executive = PojoConfig.create(ArpaoExecutive.class, props);

		System.out.println("Arpao Executive started with settings:");
		for (Field f : executive.getClass().getDeclaredFields()) {
			Parameter p = f.getAnnotation(Parameter.class);
			if (p != null) {
				Object value = f.get(executive);
				if (value instanceof String[])
					value = String.join(", ", ((String[]) value));
				System.out.println(f.getName() + "=" + value);
			}
		}
		System.out.println();

		executive.setup();
		executive.connect(executive.host, executive.port);
		executive.join();
		executive.launch();
	}
}
