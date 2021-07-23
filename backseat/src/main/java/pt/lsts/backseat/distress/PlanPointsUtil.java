package pt.lsts.backseat.distress;

import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.FollowPath;
import pt.lsts.imc4j.msg.Goto;
import pt.lsts.imc4j.msg.Loiter;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PathPoint;
import pt.lsts.imc4j.msg.PlanDB;
import pt.lsts.imc4j.msg.PlanManeuver;
import pt.lsts.imc4j.msg.PlanSpecification;
import pt.lsts.imc4j.msg.PlanTransition;
import pt.lsts.imc4j.util.TupleList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PlanPointsUtil {
    private static final String MAN_ID_PREFIX = "m";

    private static Random idRamdomGen = new Random();
    private static AtomicInteger reqId = new AtomicInteger(idRamdomGen.nextInt());

    private PlanPointsUtil() {
    }

    public static Goto createGotoFrom(double latDegs, double lonDegs, double depth,
            double speed, SpeedUnits speedUnit) {
        Goto man = new Goto();
        man.lat = Math.toRadians(latDegs);
        man.lon = Math.toRadians(lonDegs);
        man.z = (float) depth;
        man.z_units = depth == -1 ? ZUnits.NONE : ZUnits.DEPTH;
        man.speed = (float )speed;
        man.speed_units = speedUnit;

        return man;
    }

    public static Loiter createLoiterFrom(double latDegs, double lonDegs, double depth,
            double speed, SpeedUnits speedUnit, float radius, int durationSecs) {
        Loiter man = new Loiter();
        man.lat = Math.toRadians(latDegs);
        man.lon = Math.toRadians(lonDegs);
        man.z = (float) depth;
        man.z_units = depth == -1 ? ZUnits.NONE : ZUnits.DEPTH;
        man.speed = (float )speed;
        man.speed_units = speedUnit;

        man.type = Loiter.TYPE.LT_CIRCULAR;
        man.radius = radius;
        man.duration = durationSecs;

        return man;
    }

    public static FollowPath createFollowPathFrom(double latDegs, double lonDegs, double depth,
            double speed, SpeedUnits speedUnit, List<double[]> points, String customTupleListStr) {
        FollowPath man = new FollowPath();
        man.lat = Math.toRadians(latDegs);
        man.lon = Math.toRadians(lonDegs);
        man.z = (float) depth;
        man.z_units = depth == -1 ? ZUnits.NONE : ZUnits.DEPTH;
        man.speed = (float )speed;
        man.speed_units = speedUnit;

        man.custom = new TupleList(customTupleListStr);
        man.points = points.stream().map(p -> {
            PathPoint pathPoint = new PathPoint();
            pathPoint.x = (float) p[0];
            pathPoint.y = (float) p[1];
            pathPoint.z = (float) p[2];
            return pathPoint;
        }).collect(Collectors.toCollection(ArrayList::new));

        return man;
    }

    public static <M extends Maneuver> PlanSpecification createPlanSpecification(String planName, M... maneuvers) {
        PlanSpecification pspec = new PlanSpecification();
        pspec.plan_id = planName;

        final AtomicInteger manCounter = new AtomicInteger(0);
        ArrayList<PlanManeuver> planManList = Arrays.stream(maneuvers).map(m -> {
            PlanManeuver pm = new PlanManeuver();
            pm.maneuver_id = MAN_ID_PREFIX + manCounter.incrementAndGet();
            pm.data = m;
            return pm;
        }).collect(Collectors.toCollection(ArrayList::new));
        pspec.maneuvers = planManList;

        pspec.start_man_id = planManList.get(0).maneuver_id;

        ArrayList<PlanTransition> planTransList = new ArrayList<>();
        for (int i = 2; i <= manCounter.get(); i++) {
            PlanTransition pt = new PlanTransition();
            pt.source_man = MAN_ID_PREFIX + (i - 1);
            pt.dest_man = MAN_ID_PREFIX + i;
        }
        pspec.transitions = planTransList;

        return pspec;
    }

    public static PlanDB createPlanDBAdd(PlanSpecification planSpec) {
        PlanDB pdb = new PlanDB();
        pdb.request_id = reqId.incrementAndGet();
        pdb.plan_id = planSpec.plan_id;
        pdb.type = PlanDB.TYPE.DBT_REQUEST;
        pdb.op = PlanDB.OP.DBOP_SET;
        pdb.arg = planSpec;

        return pdb;
    }
}
