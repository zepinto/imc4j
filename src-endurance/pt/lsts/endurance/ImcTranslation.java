package pt.lsts.endurance;

import java.util.Date;

import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.SoiCommand;
import pt.lsts.imc4j.msg.SoiPlan;
import pt.lsts.imc4j.msg.SoiWaypoint;
import pt.lsts.imc4j.msg.SoiCommand.TYPE;
import pt.lsts.imc4j.util.PlanUtilities;
import pt.lsts.imc4j.util.SerializationUtils;
import pt.lsts.imc4j.msg.StateReport;

public class ImcTranslation {

	/**
	 * Update an Asset from a report message
	 * 
	 * @param report
	 *            The message used to update the asset state
	 * @param asset
	 *            The asset to be updated
	 */
	public static void update(StateReport report, Asset asset) {
		AssetState state = asset.getState();
		state.setLatitude(report.latitude);
		state.setLongitude(report.longitude);
		state.setTimestamp(report.stime * 1000);
		state.setFuel(report.fuel);
		state.getErrors().clear();
		if (report.exec_state == -4)
			state.getErrors().add("Vehicle reports error mode.");
		state.setHeading((report.heading / 65535.0) * 360);
	}

	public static Plan parse(SoiPlan spec) {
		Plan plan = new Plan("soi_" + spec.plan_id);
		int id = 1;
		for (SoiWaypoint wpt : spec.waypoints) {
			Waypoint soiWpt = new Waypoint(id++, wpt.lat, wpt.lon);
			soiWpt.setDuration(wpt.duration);
			if (wpt.eta > 0)
				soiWpt.setArrivalTime(new Date(1000 * wpt.eta));
			plan.addWaypoint(soiWpt);
		}
		return plan;
	}

	public static SoiPlan toImc(Plan p) {
		SoiPlan plan = new SoiPlan();
		if (p.getWaypoints() != null) {

			for (Waypoint wpt : p.getWaypoints()) {
				SoiWaypoint waypoint = new SoiWaypoint();
				if (wpt.getArrivalTime() != null)
					waypoint.eta = wpt.getArrivalTime().getTime() / 1000;
				else
					waypoint.eta = 0;
				waypoint.lat = wpt.getLatitude();
				waypoint.lon = wpt.getLongitude();
				waypoint.duration = wpt.getDuration();
				plan.waypoints.add(waypoint);
			}
		}
		byte[] data = plan.serializeFields();
		plan.plan_id = SerializationUtils.crc16(data, 2, data.length - 2);
		return plan;
	}

	public static Waypoint createWaypoint(int id, Maneuver man) throws Exception {
		float latitude = (float) Math.toDegrees(man.getDouble("lat"));
		float longitude = (float) Math.toDegrees(man.getDouble("lon"));
		Waypoint wpt = new Waypoint(id, latitude, longitude);

		if (man.getInteger("duration") != null)
			wpt.setDuration(man.getInteger("duration"));

		if (man.getInteger("arrival_time") != null)
			wpt.setArrivalTime(new Date(man.getInteger("arrival_time") * 1000l));
		return wpt;
	}

	public static Plan parse(PlanSpecification spec) {
		Plan plan = new Plan(spec.plan_id);
		int id = 1;
		for (Maneuver m : PlanUtilities.getFirstManeuverSequence(spec)) {
			try {
				plan.addWaypoint(createWaypoint(id++, m));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		plan.setCyclic(PlanUtilities.isCyclic(spec));

		return plan;
	}

	/**
	 * Update an AssetState from a report message
	 * 
	 * @param report
	 *            The message used to update the asset state
	 * @param state
	 *            The asset state to be updated
	 */
	public static void update(SoiCommand cmd, Asset asset) {
		if (cmd.type != TYPE.SOITYPE_SUCCESS)
			return;

		switch (cmd.command) {
		case SOICMD_EXEC:

			break;

		default:
			break;
		}

	}

}
