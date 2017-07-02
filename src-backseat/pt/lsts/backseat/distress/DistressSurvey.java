/**
 * 
 */
package pt.lsts.backseat.distress;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.backseat.distress.ais.AisCsvParser;
import pt.lsts.backseat.distress.ais.AisCsvParser.DistressPosition;
import pt.lsts.backseat.distress.net.TCPClientConnection;
import pt.lsts.backseat.distress.net.UDPConnection;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.ReportControl;
import pt.lsts.imc4j.util.AngleUtils;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

/**
 * @author pdias
 *
 */
public class DistressSurvey extends TimedFSM {

    public static final double MS_TO_KNOT = 3.6 / 1.852; // 1.9438444924406047516198704103672
    
    private enum GoSurfaceTaskEnum {
        START_OP,
        END_OP
    }

    @Parameter(description = "Speed to travel")
    double speed = 1100;
    
    @Parameter(description = "Speed units to use (RPM, m/s)")
    String speedUnits = "RPM";

    @Parameter(description = "DUNE Host Address")
    String hostAddr = "127.0.0.1";

    @Parameter(description = "DUNE Host Port (TCP)")
    int hostPort = 6003;
    
    @Parameter(description = "Minutes before termination")
    int minutesTimeout = 60;
    
    @Parameter(description = "DUNE plan to execute right after termination")
    String endPlanToUse = "rendezvous";
    
    @Parameter(description = "Maximum time underwater")
    int minsUnderwater = 15;

    @Parameter(description = "AIS Txt Host Address")
    String aisHostAddr = "127.0.0.1";

    @Parameter(description = "AIS Txt Host Port (TCP)")
    int aisHostPort = 13000;
    
    @Parameter(description = "AIS Txt UDP Host Port (UDP)")
    int aisUdpHostPort = 7878;

    @Parameter(description = "Loiter Radius")
    int loiterRadius = 15;

    @Parameter(description = "Max Depth")
    int maxDepth = 15;

    @Parameter(description = "Working Depth")
    int workingDepth = 5;

    @Parameter(description = "Survey Delta Altitude from Target")
    double surveyDeltaAltitudeFromTarget = 5;
    
    @Parameter(description = "Approach Lenght Offset")
    double approachLenghtOffset = 50;

    @Parameter(description = "Survey Side (true) or Around (false)")
    private boolean surveySideOrAround = false;

    @Parameter(description = "Target Width")
    double targetWidth = 6.3;

    @Parameter(description = "Target Lenght")
    double targetLenght = 65;

    @Parameter(description = "Comm Period (seconds)")
    long commPeriodSeconds = 5 * 60;
    
    @Parameter(description = "Delta Time for Distress Valid (milliseconds)")
    long deltaTimeMillisDistressValid = 30000;

    @Parameter(description = "Delta End Time Millis at Surface (milliseconds)")
    long deltaEndTimeMillisAtSurface = 20000;

    @Parameter(description = "Delta Dist to Adjust Approach (m)")
    double deltaDistToAdjustApproach = 20;
    
    @Parameter(description = "Report Period (s) [0 for not sending periodic report]")
    long reportPeriodSeconds = 60;    
    @Parameter(description = "Use Acoustic Report")
    private boolean useAcoustic = true;
    @Parameter(description = "Use GSM Report")
    private boolean useGSM = true;
    @Parameter(description = "Use Satellite Report")
    private boolean useSatellite = true;
    
    private TCPClientConnection aisTxtTcp = null;
    private UDPConnection aisTxtUdp = null;

    // State variables
    private long curTimeMillis = System.currentTimeMillis();
    private long reportSentMillis = -1;
    
    private FSMState stateToReturn = null;
    private double targetLatDeg = Double.NaN;
    private double targetLonDeg = Double.NaN;
    private double targetHeadingDeg = Double.NaN;

    private GoSurfaceTaskEnum goSurfaceTask = GoSurfaceTaskEnum.START_OP;
    private long atSurfaceMillis = -1;
    
    public DistressSurvey() {
        state = this::goSurfaceState;
    }

    @Override
    public void update(FollowRefState fref) {
        curTimeMillis = System.currentTimeMillis();
        if (reportPeriodSeconds > 0 && curTimeMillis - reportSentMillis > reportPeriodSeconds * 1000)
            sendReportMsg();
        
        super.update(fref);
    }
    
    public void init() {
        deadline = new Date(System.currentTimeMillis() + minutesTimeout * 60 * 1000);
        if (!endPlanToUse.isEmpty()) {
            endPlan = endPlanToUse;
            System.out.println("Will terminate by " + deadline + " and execute '" + endPlanToUse + "'");
        }
        else
            System.out.println("Will terminate by "+deadline);
        
//        aisTxtTcp = new TCPClientConnection(aisHostAddr, aisHostPort);
//        aisTxtTcp.register(this::parseAISTxtSentence);
//        aisTxtTcp.connect();
//        
//        aisTxtUdp = new UDPConnection(aisUdpHostPort);
//        aisTxtUdp.register(this::parseAISTxtSentence);
//        aisTxtUdp.connect();
    }

    @Override
    public void end() {
        try {
            if (aisTxtTcp != null)
                aisTxtTcp.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (aisTxtUdp != null)
                aisTxtUdp.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        super.end();
    }
    
    /**
     * AIX Txt Parser
     * @param sentence
     */
    private void parseAISTxtSentence(String sentence) {
        boolean res = AisCsvParser.process(sentence);
        System.out.println("Parsing AIS " + res);
    }
    
    @Periodic(value = 1000)
    private void sendAisPos() {
        EstimatedState es = get(EstimatedState.class);
        if (es == null)
            return;

        int mmsid = es.src;
        String type = "AUV";
        double[] pos = WGS84Utilities.WGS84displace(Math.toDegrees(es.lat), Math.toDegrees(es.lon), es.depth, es.x, es.y, 0);
        double latDeg = pos[0];
        double lonDeg = pos[1];
        double depth = es.depth < 0 ? Double.NaN : es.depth;
        double speedKt = Math.sqrt(es.vx * es.vx + es.vy * es.vy + es.vz * es.vz) * MS_TO_KNOT;
        double headingDeg = AngleUtils.nomalizeAngleDegrees360(Math.toDegrees(es.psi));
        double courseDeg = AngleUtils.nomalizeAngleDegrees180(Math.toDegrees(AngleUtils.calcAngle(0, 0, es.vy, es.vx)));
        double rateOfTurn = AngleUtils.nomalizeAngleDegrees180(Math.toDegrees(es.r));
        String navStatus = "0";// "n.a";
        double timeStampSecs = System.currentTimeMillis() / 1E3;
        String aisTxt = getAisPositionString(mmsid, type, latDeg, lonDeg, depth, speedKt, headingDeg, courseDeg, rateOfTurn,
                navStatus, timeStampSecs);
        boolean res = aisTxtTcp != null && aisTxtTcp.isConnected() && aisTxtTcp.send(aisTxt + "\r\n");
        if (res)
            System.out.println("Sent AIS txt pos. message: " + aisTxt);
    }

    private String getAisPositionString(int mmsid, String type, double latDeg, double lonDeg, double depth, double speedKt,
            double headingDeg, double courseDeg, double rateOfTurn, String navStatus, double timeStampSecs) {
        // "AIS,Node_Name=000000001,Node_Type=AUV,Latitude=43.603935,Longitude=9.0797591,Depth=0,Speed=22.8,Heading=0,Course=0,RateOfTurn=n.a.,Navigation_Status=n.a.,Timestamp=1498496471.09482,Number_Contacts=0";
        String aisTxt = String.format(Locale.ENGLISH, "AIS,Node_Name=%d,Node_Type=%s,Latitude=%.8f,Longitude=%.8f,Depth=%s,Speed=%.1f,"
                + "Heading=%.0f,Course=%.0f,RateOfTurn=%.1f,Navigation_Status=%s,Timestamp=%f,Number_Contacts=0",
                mmsid , type, latDeg, lonDeg, Double.isFinite(depth) ? String.format("%.1f", depth) : "n.a.", 
                speedKt, headingDeg, courseDeg, rateOfTurn, navStatus, timeStampSecs);
        return aisTxt;
    }
    
    private String getAisDistressString(double latDeg, double lonDeg, double depth, double speedKt, double headingDeg) {
        // DISTRESS_POSITION,Nationality=PT,Latitude=38.214447,Longitude=-9.00789,Depth=336,Speed=7.2,Heading=44.6
        String aisTxt = String.format(Locale.ENGLISH,
                "DISTRESS_POSITION,Nationality=PT,Latitude=%.8f,Longitude=%.8f,Depth=%s,Speed=%.1f,Heading=%.0f",
                latDeg, lonDeg, Double.isFinite(depth) ? String.format(Locale.ENGLISH, "%.1f", depth) : "n.a.", speedKt, headingDeg);
        return aisTxt;
    }
    
    @Override
    public void setDepth(double depth) {
        super.setDepth(Math.min(maxDepth, depth));
    }

    private void setCourseSpeed() {
        if (speedUnits.equalsIgnoreCase("rpm"))
            setSpeed(speed, SpeedUnits.RPM);
        else
            setSpeed(speed, SpeedUnits.METERS_PS);
    }
    
    private void setSurveySpeed() {
        setCourseSpeed();
    }

    private void setSurfaceLoiterRef() {
        setSurfaceLoiterRef(Double.NaN, Double.NaN);
    }

    private void setSurfaceLoiterRef(double latDeg, double lonDeg) {
        setLoiterRef(latDeg, lonDeg, 0);
    }

    private void setLoiterRef(double latDeg, double lonDeg, double depth) {
        setGoingRef(latDeg, lonDeg, depth);
        setLoiterRadius(loiterRadius);
    }

    private void setGoingRef(double latDeg, double lonDeg, double depth) {
        if (Double.isFinite(latDeg) && Double.isFinite(lonDeg))
            setLocation(latDeg, lonDeg);
        if (Double.isFinite(depth))
            setDepth(depth);
        setLoiterRadius(0);
    }

    private void sendReportMsg() {
        print("Sending position report");
        EnumSet<ReportControl.COMM_INTERFACE> itfs = EnumSet.noneOf(ReportControl.COMM_INTERFACE.class);
        if (useAcoustic)
            itfs.add(ReportControl.COMM_INTERFACE.CI_ACOUSTIC);
        if (useGSM && !isUnderwater())
            itfs.add(ReportControl.COMM_INTERFACE.CI_GSM);
        if (useSatellite && !isUnderwater())
            itfs.add(ReportControl.COMM_INTERFACE.CI_SATELLITE);
        sendReport(itfs);
        reportSentMillis = curTimeMillis;
    }
    
    private double[] calcApproachPoint(double latDegs, double lonDegs, double depth, double headingDegs, double speedKnots,
            long timestamp) {
        double angRads = Math.toRadians(headingDegs);
        double ol = targetLenght + approachLenghtOffset;
        double ow = -(targetWidth + surveyDeltaAltitudeFromTarget * 10);
        double offsetX = Math.cos(angRads) * ol + Math.sin(angRads) * ow;
        double offsetY = Math.sin(angRads) * ol + Math.cos(angRads) * ow;
        double[] pos = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);

        double latDegsRef = pos[0];
        double lonDegsRef = pos[1];
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);
        
        double[] posRef = new double[] { latDegsRef, lonDegsRef, depthRef };
        return posRef;
    }

    private double[] calcSurveyLinePoint(double latDegs, double lonDegs, double depth, double headingDegs,
            double speedKnots, long timestamp) {
        double angRads = Math.toRadians(headingDegs);
        double ol = -targetLenght;
        double ow = -(targetWidth + surveyDeltaAltitudeFromTarget * 10);
        double offsetX = Math.cos(angRads) * ol + Math.sin(angRads) * ow;
        double offsetY = Math.sin(angRads) * ol + Math.cos(angRads) * ow;
        double[] pos = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);

        double latDegsRef = pos[0];
        double lonDegsRef = pos[1];
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);
        
        double[] posRef = new double[] { latDegsRef, lonDegsRef, depthRef };
        return posRef;
    }

    private double[] calcSurveyLinePoint2(double latDegs, double lonDegs, double depth, double headingDegs,
            double speedKnots, long timestamp) {
        double angRads = Math.toRadians(headingDegs);
        double ol = -targetLenght;
        double ow = targetWidth + surveyDeltaAltitudeFromTarget * 10;
        double offsetX = Math.cos(angRads) * ol + Math.sin(angRads) * ow;
        double offsetY = Math.sin(angRads) * ol + Math.cos(angRads) * ow;
        double[] pos = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);

        double latDegsRef = pos[0];
        double lonDegsRef = pos[1];
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);
        
        double[] posRef = new double[] { latDegsRef, lonDegsRef, depthRef };
        return posRef;
    }

    private double[] calcSurveyLinePoint3(double latDegs, double lonDegs, double depth, double headingDegs,
            double speedKnots, long timestamp) {
        double angRads = Math.toRadians(headingDegs);
        double ol = targetLenght + approachLenghtOffset;
        double ow = targetWidth + surveyDeltaAltitudeFromTarget * 10;
        double offsetX = Math.cos(angRads) * ol + Math.sin(angRads) * ow;
        double offsetY = Math.sin(angRads) * ol + Math.cos(angRads) * ow;
        double[] pos = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);

        double latDegsRef = pos[0];
        double lonDegsRef = pos[1];
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);
        
        double[] posRef = new double[] { latDegsRef, lonDegsRef, depthRef };
        return posRef;
    }

    public FSMState waitState(FollowRefState ref) {
        printFSMStateName("waitState");
        return this::waitState;
    }
    
    public FSMState goSurfaceState(FollowRefState ref) {
        printFSMStateName("goSurfaceState");
        double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
        setSurfaceLoiterRef(loiterPos[0], loiterPos[1]);
        setCourseSpeed();

        atSurfaceMillis = -1;

        switch (goSurfaceTask) {
            case END_OP:
                break;
            case START_OP:
            default:
                break;
        }

        return this::goSurfaceStayState;
    }

    public FSMState goSurfaceStayState(FollowRefState ref) {
        printFSMStateName("goSurfaceStayState");
        if (isUnderwater()) {
            atSurfaceMillis = -1;
            return this::goSurfaceStayState;
        }

        if (atSurfaceMillis == -1)
            atSurfaceMillis = curTimeMillis;
        
        switch (goSurfaceTask) {
            case END_OP:
                if (hasGps() || curTimeMillis - atSurfaceMillis > deltaEndTimeMillisAtSurface) {
                    sendReportMsg();
                    end();
                }
                break;
            case START_OP:
            default:
                if (isDistressKnownToStart())
                    return this::approachSurveyPointState;
                break;
        }
        
        return this::goSurfaceStayState;
    }

    public FSMState approachSurveyPointState(FollowRefState ref) {
        printFSMStateName("approachSurveyPointState");
        DistressPosition dp = AisCsvParser.distressPosition;
        double[] posRef = calcApproachPoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        setGoingRef(posRef[0], posRef[1], posRef[2]);
        setCourseSpeed();
        
        return this::approachSurveyPointStayState;
    }

    public FSMState approachSurveyPointStayState(FollowRefState ref) {
        printFSMStateName("approachSurveyPointStayState");
        DistressPosition dp = AisCsvParser.distressPosition;
        double[] newPosRef = calcApproachPoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        double distToNewRef = WGS84Utilities.distance(Math.toDegrees(ref.reference.lat), Math.toDegrees(ref.reference.lon), newPosRef[0], newPosRef[1]);
        EstimatedState estState = get(EstimatedState.class);
        double[] curPos = WGS84Utilities.toLatLonDepth(estState);
        double distToRef = WGS84Utilities.distance(curPos[0], curPos[1], newPosRef[0], newPosRef[1]);
        
        if (distToNewRef > deltaDistToAdjustApproach) {
            setGoingRef(newPosRef[0], newPosRef[1], newPosRef[2]);
            return this::approachSurveyPointStayState;
        }
        
        if (arrivedXY()) {
            return this::firstSurveyPointState;
        }
            
        return this::approachSurveyPointStayState;
    }

    public FSMState firstSurveyPointState(FollowRefState ref) {
        printFSMStateName("firstSurveyPointState");
        DistressPosition dp = AisCsvParser.distressPosition;
        double[] posRef = calcSurveyLinePoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        setGoingRef(posRef[0], posRef[1], posRef[2]);
        setSurveySpeed();
        
        return this::firstSurveyPointStayState;
    }

    public FSMState firstSurveyPointStayState(FollowRefState ref) {
        printFSMStateName("firstSurveyPointStayState");
        DistressPosition dp = AisCsvParser.distressPosition;
        double[] newPosRef = calcSurveyLinePoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        double distToNewRef = WGS84Utilities.distance(Math.toDegrees(ref.reference.lat), Math.toDegrees(ref.reference.lon), newPosRef[0], newPosRef[1]);
        EstimatedState estState = get(EstimatedState.class);
        double[] curPos = WGS84Utilities.toLatLonDepth(estState);
        double distToRef = WGS84Utilities.distance(curPos[0], curPos[1], newPosRef[0], newPosRef[1]);

        if (distToNewRef > deltaDistToAdjustApproach) {
            setGoingRef(newPosRef[0], newPosRef[1], newPosRef[2]);
            return this::firstSurveyPointStayState;
        }
        
        if (arrivedXY()) {
            if (surveySideOrAround) {
                goSurfaceTask = GoSurfaceTaskEnum.END_OP; 
                return this::goSurfaceState;
            }
            else {
                goSurfaceTask = GoSurfaceTaskEnum.END_OP; 
                return this::goSurfaceState;
            }
        }
        
        return this::firstSurveyPointStayState;
    }

    private boolean isDistressKnownToStart() {
        DistressPosition dp = AisCsvParser.distressPosition;
        if (dp == null || System.currentTimeMillis() - dp.timestamp > deltaTimeMillisDistressValid)
            return false;
        
        return true;
    }

    private double tmpLat = 41.1815;
    private double tmpLon = -8.7051;
    private double tmpDepth = 10;
    private double tmpHeading = 20;
    private double tmpSpeedKt = 0.5;
    private long tmpLastSendTime = -1;
    private Random tmpRandom = new Random();
    @Periodic(value = 10000)
    private void sendDistressTest() {
//        double lat = 41.1815;
//        double lon = -8.7051;
//        double depth = 10;
//        double heading = 20;
//        double speedKt = 0;

        if (tmpLastSendTime > 0) {
            long deltaTMillis = System.currentTimeMillis() - tmpLastSendTime;
            double deltaTMeters = tmpSpeedKt / MS_TO_KNOT * deltaTMillis / 1E3;
            double angRads = Math.toRadians(tmpHeading);
            double offsetX = Math.cos(angRads) * deltaTMeters;
            double offsetY = Math.sin(angRads) * deltaTMeters;
            double[] pos = WGS84Utilities.WGS84displace(tmpLat, tmpLon, 0, offsetX, offsetY, 0);
            tmpLat = pos[0];
            tmpLon = pos[1];
            tmpHeading += tmpRandom.nextGaussian() * 2;
        }
        
        String dis = getAisDistressString(tmpLat, tmpLon, tmpDepth, tmpSpeedKt, tmpHeading);
        parseAISTxtSentence(dis + "\r\n");
        
        String aisPos = getAisPositionString(1000022, "Submarine", tmpLat, tmpLon, tmpDepth, tmpSpeedKt, tmpHeading,
                tmpHeading, 0, "0", System.currentTimeMillis());
        byte[] buf = aisPos.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(aisHostAddr), aisUdpHostPort);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        tmpLastSendTime = System.currentTimeMillis();
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: java -jar Distress.jar <CONF_FILE>");
            System.exit(1);
        }
        
        File file = new File(args[0]);
        if (!file.exists()) {
            PojoConfig.writeProperties(new DistressSurvey(), file);
            System.exit(0);         
        }
        
        Properties props = new Properties();
        props.load(new FileInputStream(file));
                
        DistressSurvey tracker = PojoConfig.create(DistressSurvey.class, props);
        tracker.init();

        System.out.println("Distress Survey started with settings:");
        for (Field f : tracker.getClass().getDeclaredFields()) {
            Parameter p = f.getAnnotation(Parameter.class);
            if (p != null) {
                System.out.println(f.getName() + "=" + f.get(tracker));
            }
        }
        System.out.println();

        tracker.connect(tracker.hostAddr, tracker.hostPort);
        tracker.join();
    }
}
