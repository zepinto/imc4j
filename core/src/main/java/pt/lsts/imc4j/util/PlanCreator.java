package pt.lsts.imc4j.util;

import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.def.ZUnits;
import pt.lsts.imc4j.msg.*;
import pt.lsts.imc4j.net.ImcNetwork;
import java.util.ArrayList;
import java.util.List;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PlanCreator {
    private double latitude = 0;
    private double longitude = 0;
    private double rotation = 0;
    private double speed = 1;
    private double depth = 2;
    private List<Waypoint> coords = new ArrayList<>();

    public PlanCreator() {

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public List<Waypoint> getCoords() {
        return coords;
    }

    public void addPoint(double x, double y, double z) {
        Waypoint wpt = new Waypoint(x,y,z,0);
        coords.add(wpt);

    }

    public void addPoint(double x, double y, double z, double duration) {
        Waypoint wpt = new Waypoint(x,y,z,duration);
        coords.add(wpt);        
    }

    PlanSpecification createPlan(String id) {
        PlanSpecification plan = new PlanSpecification();
        plan.plan_id = id;
        int count = 1;
        for (Waypoint wpt : coords) {
            double ned[] = new double[3];
            ned[0] = Math.cos(Math.toRadians(rotation)) * wpt.x - Math.sin(Math.toRadians(rotation)) * wpt.y;
            ned[1] = Math.cos(Math.toRadians(rotation)) * wpt.y + Math.sin(Math.toRadians(rotation)) * wpt.x;            
            ned[2] = wpt.z;

            System.out.println(ned[0]+","+ned[1]+","+ned[2]);
            double lld[] = WGS84Utilities.WGS84displace(latitude, longitude, 0, ned[0], ned[1], ned[2]);
            
            if (wpt.duration == 0) {
                Goto man = new Goto();
                man.lat = Math.toRadians(lld[0]);
                man.lon = Math.toRadians(lld[1]);
                man.z_units = ned[2] >= 0? ZUnits.DEPTH : ZUnits.ALTITUDE;
                man.z = (float) Math.abs(ned[2]);
                man.speed = (float)speed;
                man.speed_units = SpeedUnits.METERS_PS;

                PlanManeuver pm = new PlanManeuver();
                pm.data = man;
                pm.maneuver_id = String.format("wpt%02d", count++);
                plan.maneuvers.add(pm);
            }
            else if (ned[2] == 0){
                StationKeeping man = new StationKeeping();
                man.lat = Math.toRadians(lld[0]);
                man.lon = Math.toRadians(lld[1]);
                man.z_units = ZUnits.DEPTH;
                man.z = 0;
                man.speed = (float)speed;
                man.speed_units = SpeedUnits.METERS_PS;
                man.duration = (int) wpt.duration;
                PlanManeuver pm = new PlanManeuver();
                pm.data = man;
                pm.maneuver_id = String.format("wpt%02d", count++);
                plan.maneuvers.add(pm);
            }
            else {
                Loiter man = new Loiter();
                man.lat = Math.toRadians(lld[0]);
                man.lon = Math.toRadians(lld[1]);
                man.z_units = ned[2] >= 0? ZUnits.DEPTH : ZUnits.ALTITUDE;
                man.z = (float) Math.abs(ned[2]);
                man.duration = (int) wpt.duration;
                man.radius = 15;
                man.speed = (float)speed;
                man.speed_units = SpeedUnits.METERS_PS;
                PlanManeuver pm = new PlanManeuver();
                pm.data = man;
                pm.maneuver_id = String.format("wpt%02d", count++);
                plan.maneuvers.add(pm);
            }
        }

        plan.start_man_id = plan.maneuvers.get(0).maneuver_id;
        for (int i = 0; i < plan.maneuvers.size()-1; i++) {
            PlanTransition transition = new PlanTransition();
            transition.source_man = plan.maneuvers.get(i).maneuver_id;
            transition.dest_man = plan.maneuvers.get(i+1).maneuver_id;
            transition.conditions = "maneuverIsDone";
            plan.transitions.add(transition);
        }

        return plan;
    }

    public void printPlan() {
        System.out.println(createPlan("plan").toString());
    }

    public void commandPlan(String vehicle) throws Exception {
        ImcNetwork network = new ImcNetwork("PlanCreator", 9000, SystemType.CCU);
        network.setConnectionPolicy(p -> p.getName().equals(vehicle));
        network.startListening(9000);
        network.bind(Message.class, m -> {
            System.out.println(m.abbrev());
        });
        while (true) {
            try {
                network.peer(vehicle);
                break;
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
                Thread.sleep(10_000);
            }
        }

        PlanControl pc = new PlanControl();
        pc.type = PlanControl.TYPE.PC_REQUEST;
        pc.op = PlanControl.OP.PC_START;
        pc.plan_id = "PCreator_"+System.currentTimeMillis()/1000;
        pc.arg = createPlan(pc.plan_id);
        network.peer(vehicle).send(pc);
        System.exit(0);
    }

    public void setCoords(List<Waypoint> coords) {
        this.coords = coords;
    }

    static class Waypoint {

        double x;
        double y;
        double z;
        double duration;

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getZ() {
            return z;
        }

        public void setZ(double z) {
            this.z = z;
        }

        public double getDuration() {
            return duration;
        }

        public void setDuration(double duration) {
            this.duration = duration;
        }

        public Waypoint(double x, double y, double z, double duration) {
            this.duration = duration;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String toString() {            
            return String.format("wpt(%f,%f,%f,%f)",x,y,z,duration);
        }
    }

    public static void command(Path path) throws Exception {

        ArrayList<String> lines = new ArrayList<>();


        Files.readAllLines(path).forEach(l -> {
            String line = l.trim().toLowerCase();
            if (line.isEmpty() || line.startsWith("#"))
                return;

            lines.add(line);
        });
        PlanCreator creator = new PlanCreator();
        String vehicle = lines.remove(0);
        creator.setLatitude(Double.valueOf(lines.remove(0)));
        creator.setLongitude(Double.valueOf(lines.remove(0)));
        creator.setRotation(Double.valueOf(lines.remove(0)));
        creator.setSpeed(Double.valueOf(lines.remove(0)));

        while (!lines.isEmpty()) {
            String[] parts = lines.remove(0).split("\\s");
            if (parts.length == 3)
                creator.addPoint(
                        Double.valueOf(parts[0]),
                        Double.valueOf(parts[1]),
                        Double.valueOf(parts[2])
                );
            else
                creator.addPoint(
                        Double.valueOf(parts[0]),
                        Double.valueOf(parts[1]),
                        Double.valueOf(parts[2]),
                        Double.valueOf(parts[3])
                );
        }
        creator.commandPlan(vehicle);
    }

    public static void createTemplate(File f) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));

        writer.write("#Vehicle to command the plan to\n");
        writer.write("lauv-xplore-1\n");
        writer.write("\n");

        writer.write("#Origin latitude (in degrees)\n");
        writer.write("41.185\n");
        writer.write("\n");

        writer.write("#Origin longitude (in degrees)\n");
        writer.write("-8.706\n");
        writer.write("\n");

        writer.write("#Yaw rotation (in degrees)\n");
        writer.write("-130.0\n");
        writer.write("\n");

        writer.write("#Vehicle speed (in m/s)\n");
        writer.write("1.0\n");
        writer.write("\n");

        writer.write("#List of waypoints in the form x y z duration\n");
        writer.write("0 0 0 0\n");
        writer.write("100 0 0 0\n");
        writer.write("100 100 0 0\n");
        writer.write("0 100 0 0\n");
        writer.write("0 0 0 60\n");

        writer.close();
    }

    public static void main(String[] args) throws Exception {
        args = new String[] {"/home/zp/Desktop/eumr-tna/plan.txt"};
        if (args.length == 0) {
            System.err.println("Usage ./pcreator [filename]");
            System.exit(1);
        }
        File f = new File(args[0]);
        if (!f.exists()) {
            PlanCreator.createTemplate(f);
            System.out.println("Example plan template created in "+f.getName()+".");
            System.exit(0);
        }
        PlanCreator.command(f.toPath());
    }
}
