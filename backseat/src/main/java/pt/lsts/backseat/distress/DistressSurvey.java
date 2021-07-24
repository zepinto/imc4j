/**
 * 
 */
package pt.lsts.backseat.distress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.backseat.distress.ais.AisCsvParser;
import pt.lsts.backseat.distress.ais.AisCsvParser.DistressPosition;
import pt.lsts.backseat.distress.net.TCPClientConnection;
import pt.lsts.backseat.distress.net.UDPConnection;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.def.SpeedUnits;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.msg.Maneuver;
import pt.lsts.imc4j.msg.PlanDB;
import pt.lsts.imc4j.msg.ReportControl;
import pt.lsts.imc4j.util.AngleUtils;
import pt.lsts.imc4j.util.ManeuversUtil;
import pt.lsts.imc4j.util.PointIndex;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

/**
 * @author pdias
 *
 */
public class DistressSurvey extends TimedFSM {

    public static final double MS_TO_KNOT = 3.6 / 1.852; // 1.9438444924406047516198704103672
    @SuppressWarnings("unused")
    private static final String PAYLOAD_TO_ACTIVATE_PATTERN = "(?i)(payload *?=[\\w- \\[\\]\\(\\)]+)((\\;)[\\w- \\[\\]\\(\\)]+)=[\\w- \\[\\]\\(\\)]+))*(\\; *)";

    public static final String PLAN_PATTERN_SUFFIX = "pattern";
    public static final String PLAN_REF_SUFFIX = "ref";

    private enum GoSurfaceTaskEnum {
        START_OP,
        END_OP,
        PREEMPTIVE_OP
    }

    private enum SurveyStageEnum {
        WAITING_TO_START,
        ON_GOING
    }

    private enum ApproachCornerEnum {
        FRONT_LEFT,
        FRONT_RIGHT,
        BACK_RIGHT,
        BACK_LEFT
    }

    private enum SurveyPatternEnum {
        DEFAULT(0, "default"),
        ROWS(1, "rows"),
        RI(2, "ri"),
        CROSS(3, "cross"),
        EXPANDING(4, "expanding"),
        ;

        public final int ordinal;
        public final String pattern;

        SurveyPatternEnum(int ordinal, String pattern) {
            this.ordinal = ordinal;
            this.pattern = pattern;
        }
    }

    @Parameter(description = "DUNE Host Address")
    private String hostAddr = "127.0.0.1";
    @Parameter(description = "DUNE Host Port (TCP)")
    private int hostPort = 6006;
    @Parameter(description = "DUNE plan to execute right after termination")
    private String endPlanToUse = "rendezvous";

    @Parameter(description = "Name prefix for created plans")
    private String planCreationPrefix = "dissub-";

    @Parameter(description = "Minutes before termination")
    private int minutesTimeout = 60;
    
    @Parameter(description = "Maximum time underwater (minutes)")
    private int minsUnderwater = 15;
    @Parameter(description = "Periodic surface for position")
    private boolean usePeriodicSurfaceForPos = false;
    @Parameter(description = "Surface on corners")
    private boolean surfaceOnCorners = false;
    
    @Parameter(description = "Wait Distress Underwater")
    private boolean waitDistressUnderwater = false;
    @Parameter(description = "Wait Distress Underwater Periodic Resurface (minutes)")
    private int waitDistressUnderwaterPeriodicResurfaceMinutes = 10;
    
    @Parameter(description = "Keep in Parking Pos at Start")
    private boolean keepInParkingPosAtStart = true;

    @Parameter(description = "AIS Txt Host Address")
    private String aisHostAddr = "127.0.0.1";
    @Parameter(description = "AIS Txt by TCP")
    private boolean aisByTCP = true;
    @Parameter(description = "AIS Txt Host Port (TCP)")
    private int aisHostPort = 13000;
    @Parameter(description = "AIS Txt by UDP")
    private boolean aisByUDP = false;
    @Parameter(description = "AIS Txt UDP Host Port (UDP)")
    private int aisUdpHostPort = 7879;
    
    @Parameter(description = "AIS UDP ReTransmit")
    private boolean aisUDPReTransmit = false;
    @Parameter(description = "AIS UDP ReTransmit Host")
    private String aisUDPReTransmitHost = "127.0.0.1";
    @Parameter(description = "AIS UDP ReTransmit Port")
    private int aisUDPReTransmitPort = 7878;

    @Parameter(description = "Loiter Radius (m)")
    private int loiterRadius = 15;
    @Parameter(description = "Max Depth (m)")
    private int maxDepth = 15;
    @Parameter(description = "Working Depth (m)")
    private int workingDepth = 5;
    @Parameter(description = "Speed to travel")
    private double speed = 1400;
    @Parameter(description = "Speed units to use (RPM, m/s)")
    private String speedUnits = "RPM";

    @Parameter(description = "Survey Delta Altitude from Target (m)")
    private double surveyDeltaAltitudeFromTarget = 5;
    @Parameter(description = "Approach Length Offset")
    private double approachLengthOffset = 50;
    @Parameter(description = "Survey Side (true) or Around Target (false) (only for default pattern)")
    private boolean surveySideOrAround = false;

    @Parameter(description = "Survey pattern type [default|rows|ri|cross|expanding]")
    private String surveyPatternName = "default";

    @Parameter(description = "Target Width")
    private double targetWidth = 6.3;
    @Parameter(description = "Target Length")
    private double targetLength = 65;

    @Parameter(description = "Delta Time for Distress Valid (milliseconds)")
    private long deltaTimeMillisDistressValid = 30000;
    @Parameter(description = "Delta End Time Millis at Surface (milliseconds)")
    private long deltaEndTimeMillisAtSurface = 20000;
    @Parameter(description = "Delta Dist to Adjust Approach (m)")
    private double deltaDistToAdjustApproach = 20;
    
    @Parameter(description = "Report Period (s) [0 for not sending periodic report]")
    private long reportPeriodSeconds = 60;    
    @Parameter(description = "Use Acoustic Report")
    private boolean useAcoustic = true;
    @Parameter(description = "Use GSM Report")
    private boolean useGSM = true;
    @Parameter(description = "Use Satellite Report")
    private boolean useSatellite = true;

    @Parameter(description = "Test Target Simulate")
    private boolean testTargetSimulate = false;
    @Parameter(description = "Test Target Lat (decimal degs)")
    private double testTargetLat = 41.184058;
    @Parameter(description = "Test Target Lon (decimal degs)")
    private double testTargetLon = -8.706333;
    @Parameter(description = "Test Target Depth (m)")
    private double testTargetDepth = 10;
    @Parameter(description = "Test Target Heading (degs)")
    private double testTargetHeading = 320;
    @Parameter(description = "Test Target Heading Gaussian Noise (degs)")
    private double testTargetHeadingGaussianNoiseDegs = 2;
    @Parameter(description = "Test Target Speed (knots)")
    private double testTargetSpeedKt = 0.5;
    
    @Parameter(description = "Use payload")
    private boolean usePayload = false;
    @Parameter(description = "(Payload=<Payload Name>(;<Param Name>=<Param Value>)*)*")
    private String activatePayload = "Payload=Sidescan; High-Frequency Channels=Both; High-Frequency Range=75; "
            + "Low-Frequency Channels=Both; Low-Frequency Range=150; Range Multiplier=2;"
            + "Payload=Camera;";

    private TCPClientConnection aisTxtTcp = null;
    private UDPConnection aisTxtUdp = null;

    // State variables
    private long curTimeMillis = System.currentTimeMillis();
    private long reportSentMillis = -1;
    private long atSurfaceMillis = -1;
    private long atWaitingUnderwaterMillis = -1;
    
    private FSMState stateToReturnTo = null;

    private GoSurfaceTaskEnum goSurfaceTask = GoSurfaceTaskEnum.START_OP;
    private ApproachCornerEnum approachCorner = ApproachCornerEnum.FRONT_LEFT;
    private SurveyStageEnum surveyStage = SurveyStageEnum.WAITING_TO_START;

    private PointIndex surfacePointIdx = new PointIndex(3);

    // Target test vars
    private double tmpTargetLat = 41.184058;
    private double tmpTargetLon = -8.706333;
    private double tmpTargetDepth = 10;
    private double tmpTargetHeading = 320;
    private double tmpTargetSpeedKt = 0.5;
    private long tmpTargetLastSendTime = -1;
    private Random tmpTargetRandom = new Random();
    
    private double latDegParking = Double.NaN;
    private double lonDegParking = Double.NaN;

    private SurveyPatternEnum surveyPattern = SurveyPatternEnum.DEFAULT;

    private HashMap<String, HashMap<String, String>> payloadToActivate = new LinkedHashMap<>();

    private double patternLatDegsForPlan = Double.NaN;
    private double patternLonDegsForPlan = Double.NaN;
    private double patternDepthForPlan = Double.NaN;
    private List<double[]> patternPathOffsetsForPlan = new ArrayList<>();

    private int lastSentPlanRefHash = -1;
    private int lastSentPlanPatternHash = -1;

    public DistressSurvey() {
        if (waitDistressUnderwater) {
            stateToReturnTo = this::loiterUnderwaterState;
            goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
        }
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
            print("Will terminate by " + deadline + " and execute '" + endPlanToUse + "'");
        }
        else {
            print("Will terminate by " + deadline);
        }

        if (aisByTCP) {
            aisTxtTcp = new TCPClientConnection(aisHostAddr, aisHostPort);
            aisTxtTcp.register(this::parseAISTxtSentence);
            aisTxtTcp.register(this::retransmitAISTxtSentence);
            aisTxtTcp.connect();
        }
        
        if (aisByUDP) {
            aisTxtUdp = new UDPConnection(aisUdpHostPort);
            aisTxtUdp.register(this::parseAISTxtSentence);
            aisTxtUdp.connect();
        }
        
        if (waitDistressUnderwater) {
            stateToReturnTo = this::loiterUnderwaterState;
            goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
        }

        switch (surveyPatternName.trim().toLowerCase()) {
            case "default":
            default:
                surveyPattern = SurveyPatternEnum.DEFAULT;
                break;
            case "rows":
            case "row":
                surveyPattern = SurveyPatternEnum.ROWS;
                break;
            case "ri":
            case "ripattern":
            case "ri-pattern":
                surveyPattern = SurveyPatternEnum.RI;
                break;
            case "cross":
            case "crosshatch":
                surveyPattern = SurveyPatternEnum.CROSS;
                break;
            case "expanding":
            case "expandingsquare":
            case "expanding-square":
                surveyPattern = SurveyPatternEnum.EXPANDING;
                break;
        }

        processPayloadToActivate();
    }

    @Override
    public void end() {
        deactivatePayload();
        
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

    private void processPayloadToActivate() {
        payloadToActivate.clear();

        if (activatePayload != null && activatePayload.length() >= 3) {
            try {
                String[] itemsList = activatePayload.split(" *; *");
                LinkedHashMap<String, String> paramValue = null;
                for (String it : itemsList) {
                    String[] paramNameTk = it.split(" *= *");
                    if (paramNameTk.length != 2)
                        continue;

                    try {
                        String name = paramNameTk[0].trim();
                        String value = paramNameTk[1].trim();

                        if ("payload".equalsIgnoreCase(name)) {
                            paramValue = new LinkedHashMap<>();
                            payloadToActivate.put(value, paramValue);
                        }
                        else if (paramValue != null) {
                            paramValue.put(name, value);
                        }
                    }
                    catch (Exception e) {
                        printError(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e) {
                printError(e.getMessage());
                e.printStackTrace();
            }
        }

        print(getPayloadParseAsString());
    }

    private String getPayloadParseAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Payloads Set:");
        for (String ent : payloadToActivate.keySet()) {
            sb.append("\n  Payload: ").append(ent).append(" {");
            HashMap<String, String> params = payloadToActivate.get(ent);
            for (String v : params.keySet()) {
                sb.append("\n    ").append(v).append("=").append(params.get(v)).append(";");
            }
            sb.append("\n  }");
        }
        sb.append("\n End Payloads Set");
        return sb.toString();
    }

    private void activatePayload() {
        String pls = "";
        if (usePayload && !payloadToActivate.isEmpty()) {
            for (String pl : payloadToActivate.keySet()) {
                HashMap<String, String> pv = payloadToActivate.get(pl);
                if (pv != null && !pv.isEmpty()) {
                    pls += pl + " (no params);";
                    activate(pl);
                }
                else {
                    pls += pl + ";";
                    StringBuilder paramValue = new StringBuilder();
                    pv.keySet().stream().forEach(k -> paramValue.append(pv).append("=").append(pv.get(k)));
                    activate(pl, paramValue.toString());
                }
            }
            print("Activating: " + pls);
        }
        else {
            print("NOT activating: " + pls);
        }
    }
    private void deactivatePayload() {
        String pls = "";
        if (usePayload && !payloadToActivate.isEmpty()) {
            for (String pl : payloadToActivate.keySet()) {
                deactivate(pl);
                pls += pl + ";";
            }
            print("Deactivating: " + pls);
        }
        else {
            print("NOT deactivating: " + pls);
        }
    }

    /**
     * AIX Txt Parser
     * @param sentence
     */
    private void parseAISTxtSentence(String sentence) {
        boolean res = AisCsvParser.process(sentence);
        print("Parsing AIS " + res + "  >> " + sentence);
    }

    private void retransmitAISTxtSentence(String sentence) {
        if (!aisUDPReTransmit)
            return;
            
        byte[] buf = sentence.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(aisUDPReTransmitHost),
                    aisUDPReTransmitPort);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Periodic(value = 5000)
    private void sendAisPos() {
        EstimatedState es = get(EstimatedState.class);
        if (es == null)
            return;

        String mmsid = "" + es.src;
        Announce announceMsg = get(Announce.class);
        if (announceMsg != null) {
            mmsid = announceMsg.sys_name;
        }
        
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

    private String getAisPositionString(String mmsid, String type, double latDeg, double lonDeg, double depth, double speedKt,
            double headingDeg, double courseDeg, double rateOfTurn, String navStatus, double timeStampSecs) {
        // "AIS,Node_Name=000000001,Node_Type=AUV,Latitude=43.603935,Longitude=9.0797591,Depth=0,Speed=22.8,Heading=0,Course=0,RateOfTurn=n.a.,Navigation_Status=n.a.,Timestamp=1498496471.09482,Number_Contacts=0";
        String aisTxt = String.format(Locale.ENGLISH, "AIS,Node_Name=%s,Node_Type=%s,Latitude=%.8f,Longitude=%.8f,Depth=%s,Speed=%.1f,"
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

    private double getCourseSpeedValue() {
        return speed;
    }

    private SpeedUnits getCourseSpeedUnit() {
        if (speedUnits.equalsIgnoreCase("rpm"))
            return SpeedUnits.RPM;
        else
            return SpeedUnits.METERS_PS;
    }

    private void setSurveySpeed() {
        setCourseSpeed();
    }

    private double getSurveySpeedValue() {
        return speed;
    }

    private SpeedUnits getSurveySpeedUnit() {
        return getCourseSpeedUnit();
    }

    @SuppressWarnings("unused")
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
    
    private ApproachCornerEnum calcApproachCorner(double latDegs, double lonDegs, double depth,
            double headingDegs, double speedKnots, long timestamp) {
        double[] vehPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
        ApproachCornerEnum approachCorner = ApproachCornerEnum.FRONT_LEFT;
        double angRads = Math.toRadians(headingDegs);
        double offsetX = Math.cos(angRads) * targetLength / 2.;
        double offsetY = Math.sin(angRads) * targetLength / 2.;
        double[] frontCenterPoint = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);
        offsetX = Math.cos(angRads) * -targetLength / 2.;
        offsetY = Math.sin(angRads) * -targetLength / 2.;
        double[] backCenterPoint = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);
        
        double distToFront = WGS84Utilities.distance(vehPos[0], vehPos[1], frontCenterPoint[0], frontCenterPoint[1]);
        double distToBack = WGS84Utilities.distance(vehPos[0], vehPos[1], backCenterPoint[0], backCenterPoint[1]);
        double angleDeltaRads = 0;
        
        if (distToFront <= distToBack) {
            approachCorner = ApproachCornerEnum.FRONT_LEFT;
            angleDeltaRads = AngleUtils.calcAngle(frontCenterPoint[1], frontCenterPoint[0], vehPos[1], vehPos[0]);
            angleDeltaRads -= angRads;
            angleDeltaRads = AngleUtils.nomalizeAngleRadsPi(angleDeltaRads);
            if (angleDeltaRads > 0)
                approachCorner = ApproachCornerEnum.FRONT_RIGHT;
        }
        else {
            approachCorner = ApproachCornerEnum.BACK_RIGHT;
            angleDeltaRads = AngleUtils.calcAngle(backCenterPoint[1], backCenterPoint[0], vehPos[1], vehPos[0]);
            angleDeltaRads -= angRads;
            angleDeltaRads = AngleUtils.nomalizeAngleRadsPi(angleDeltaRads);
            if (AngleUtils.nomalizeAngleRadsPi(angleDeltaRads - Math.PI) > 0)
                approachCorner = ApproachCornerEnum.BACK_LEFT;
        }
        
        print(String.format("Approach front %.2f  back %.2f  angle %.1f  approach %s",
                distToFront, distToBack, Math.toDegrees(angleDeltaRads), approachCorner.toString()));
        
        return approachCorner;
    }
    
    private double[] calcApproachPoint(double latDegs, double lonDegs, double depth,
            double headingDegs, List<double[]> refPoints) {
        //surfacePointIdx = SurveyPathEnum.FIRST;
        surfacePointIdx.resetIndex();
        return calcSurveyLinePoint(latDegs, lonDegs, refPoints);
    }

    private List<double[]> calcSurveyLinePathOffsets(double depth, double headingDegs) {
        List<double[]> refPoints;
        switch (surveyPattern) {
            case DEFAULT:
            default:
                refPoints = calcSurveyLinePathOffsetsForDefault(depth, headingDegs, false);
                break;
            case ROWS:
                refPoints = calcSurveyLinePathOffsetsForRows(depth, headingDegs, false);
                break;
            case RI:
                refPoints = calcSurveyLinePathOffsetsForRI(depth, headingDegs, false);
                break;
            case CROSS:
                refPoints = calcSurveyLinePathOffsetsForCrossHatch(depth, headingDegs, false);
                break;
            case EXPANDING:
                refPoints = calcSurveyLinePathOffsetsForExpanding(depth, headingDegs, false);
                break;
        }

        if (lastSentPlanPatternHash != calcRefPointsHashCode(refPoints)) {
            double[] offXyzFromIdx = refPoints.get(surfacePointIdx.index());
            print(String.format("Calc survey points to %s deltas %s idx=%s  offn %.2f  offe %.2f ::  offs=-->",
                    surveyPattern.pattern, approachCorner, surfacePointIdx, offXyzFromIdx[0], offXyzFromIdx[1]));
            System.out.println(String.format("Survey %s deltas offs=%s", surveyPattern.pattern,
                    refPoints.stream().map(o -> Arrays.toString(o)).collect(Collectors.joining())));
        }

        surfacePointIdx.resetMinMax(0, refPoints.size() -1);
        return refPoints;
    }

    private List<double[]> calcSurveyLinePathOffsetsForDefault(double depth, double headingDegs, boolean printResult) {
        double angRads = Math.toRadians(headingDegs);

        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);

        ArrayList<Double> olPointsList = new ArrayList<>();
        ArrayList<Double> owPointsList = new ArrayList<>();
        // FrontLeft
        olPointsList.add(targetLength); // + approachLengthOffset;
        owPointsList.add(-(targetWidth + surveyDeltaAltitudeFromTarget * 10));
        // BackLeft
        olPointsList.add(-targetLength);
        owPointsList.add(-(targetWidth + surveyDeltaAltitudeFromTarget * 10));
        // BackRight
        olPointsList.add(-targetLength);
        owPointsList.add(targetWidth + surveyDeltaAltitudeFromTarget * 10);
        // FrontRight
        olPointsList.add(targetLength); // + approachLengthOffset;
        owPointsList.add(targetWidth + surveyDeltaAltitudeFromTarget * 10);

        double approachLengthOffsetFixed = approachLengthOffset;

        switch (approachCorner) {
            case FRONT_LEFT: // 1,2,3,4
            default:
                break;
            case BACK_LEFT: // 2,1,4,3
                Collections.reverse(olPointsList);
                Collections.reverse(owPointsList);
                Collections.rotate(olPointsList, 2);
                Collections.rotate(owPointsList, 2);
                approachLengthOffsetFixed *= -1;
                break;
            case BACK_RIGHT: // 3,4,1,2
                Collections.rotate(olPointsList, 2);
                Collections.rotate(owPointsList, 2);
                approachLengthOffsetFixed *= -1;
                break;
            case FRONT_RIGHT: // 4,3,2,1
                Collections.reverse(olPointsList);
                Collections.reverse(owPointsList);
                break;
        }

        olPointsList.set(0, olPointsList.get(0) + approachLengthOffsetFixed);
        olPointsList.set(3, olPointsList.get(3) + approachLengthOffsetFixed);

        List<double[]> refPoints = new ArrayList<>();
        for (int i = 0; i < olPointsList.size(); i++) {
            double ol = olPointsList.get(i);
            double ow = owPointsList.get(i);
            double d = i == 0 ? workingDepth : depthRef;

            double offsetX = Math.cos(angRads) * ol - Math.sin(angRads) * ow;
            double offsetY = Math.sin(angRads) * ol + Math.cos(angRads) * ow;

            refPoints.add(new double[] {offsetX, offsetY, d});
        }

        if (printResult) {
            double[] offXyzFromIdx = refPoints.get(surfacePointIdx.index());
            print(String.format("Calc survey 1 deltas %s idx=%s   l %.2f  w %.2f  offn %.2f  offe %.2f ::  l=%s  w=%s",
                    approachCorner, surfacePointIdx, olPointsList.get(surfacePointIdx.index()),
                    owPointsList.get(surfacePointIdx.index()), offXyzFromIdx[0], offXyzFromIdx[1],
                    Arrays.toString(olPointsList.toArray()), Arrays.toString(owPointsList.toArray())));
        }

        return refPoints;
    }

    private static double fixHeadingDegsForEntryBottomLeft(double headingDegs, ApproachCornerEnum approachCorner) {
        double angDegs = headingDegs;
        switch (approachCorner) {
            case FRONT_LEFT:
                angDegs += 90;
                break;
            case BACK_LEFT:
            default:
                break;
            case BACK_RIGHT:
                angDegs -= 90;
                break;
            case FRONT_RIGHT:
                angDegs += 180;
                break;
        }

        return angDegs;
    }

    private static List<double[]> addApproachAndExitPointsExtendingLines(List<double[]> refPoints,
            double headingDegs, double approachLengthOffset, double workingDepth) {
        return addApproachAndExitPointsExtendingLines(refPoints, headingDegs, approachLengthOffset,
                approachLengthOffset, workingDepth);
    }

    private static List<double[]> addApproachAndExitPointsExtendingLines(List<double[]> refPoints,
            double headingDegs, double entryApproachLengthOffset, double exitApproachLengthOffset,
            double workingDepth) {
        double angRads = Math.toRadians(headingDegs);

        double[] p0 = refPoints.get(0);
        double[] rp = AngleUtils.rotate(angRads, p0[ManeuversUtil.X], p0[ManeuversUtil.Y], true);
        rp[ManeuversUtil.X] -= entryApproachLengthOffset;
        rp = AngleUtils.rotate(angRads, rp[ManeuversUtil.X], rp[ManeuversUtil.Y], false);
        refPoints.add(new double[]{ rp[ManeuversUtil.X], rp[ManeuversUtil.Y], workingDepth });
        Collections.rotate(refPoints, 1);

        double[] p10 = refPoints.get(refPoints.size() - 2);
        double[] p11 = refPoints.get(refPoints.size() - 1);
        double[] rp10 = AngleUtils.rotate(angRads, p10[ManeuversUtil.X], p10[ManeuversUtil.Y], true);
        double[] rp11 = AngleUtils.rotate(angRads, p11[ManeuversUtil.X], p11[ManeuversUtil.Y], true);
        double p10p11AngleRad = AngleUtils.calcAngle(rp10[ManeuversUtil.X], rp10[ManeuversUtil.Y],
                rp11[ManeuversUtil.X], rp11[ManeuversUtil.Y]);
        rp11 = AngleUtils.rotate(p10p11AngleRad, rp11[ManeuversUtil.X], rp11[ManeuversUtil.Y], false,
                rp10[ManeuversUtil.X], rp10[ManeuversUtil.Y]);
        rp11[ManeuversUtil.Y] += exitApproachLengthOffset;
        rp11 = AngleUtils.rotate(p10p11AngleRad, rp11[ManeuversUtil.X], rp11[ManeuversUtil.Y], true,
                rp10[ManeuversUtil.X], rp10[ManeuversUtil.Y]);
        rp11 = AngleUtils.rotate(angRads, rp11[ManeuversUtil.X], rp11[ManeuversUtil.Y], false);
        refPoints.add(new double[]{ rp11[ManeuversUtil.X], rp11[ManeuversUtil.Y], workingDepth });

        return refPoints;
    }

    private List<double[]> calcSurveyLinePathOffsetsForRows(double depth, double headingDegs, boolean printResult) {
        double angRads = Math.toRadians(headingDegs);
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);

        boolean invertRows;
        switch (approachCorner) {
            case FRONT_LEFT:
                angRads += Math.PI;
                invertRows = true;
                break;
            case BACK_LEFT:
            default:
                invertRows = false;
                break;
            case BACK_RIGHT:
                invertRows = true;
                break;
            case FRONT_RIGHT:
                angRads += Math.PI;
                invertRows = false;
                break;
        }

        double rowsWidth = (targetWidth + surveyDeltaAltitudeFromTarget * 10) * 2;
        double rowsLength = targetLength * 1.3 + surveyDeltaAltitudeFromTarget * 10;
        List<double[]> refPoints = ManeuversUtil.calcRowsPoints(rowsWidth, rowsLength, 27, 1,
                10, true, angRads, 0, invertRows);
        refPoints.forEach(p -> p[ManeuversUtil.Z] = depthRef);

        // Because this is not a centered maneuver we need to offset it
        final double angRadsFinal = angRads;
        refPoints.forEach(p -> {
            double[] np = AngleUtils.rotate(angRadsFinal, p[ManeuversUtil.X], p[ManeuversUtil.Y], true);
            p[ManeuversUtil.X] = np[ManeuversUtil.X] - rowsLength / 2.0;
            p[ManeuversUtil.Y] = np[ManeuversUtil.Y] - (invertRows ? -1 : 1) * rowsWidth / 2.0;
            np = AngleUtils.rotate(angRadsFinal, p[ManeuversUtil.X], p[ManeuversUtil.Y], false);
            p[ManeuversUtil.X] = np[ManeuversUtil.X];
            p[ManeuversUtil.Y] = np[ManeuversUtil.Y];
        });

        refPoints = addApproachAndExitPointsExtendingLines(refPoints, Math.toDegrees(angRads), approachLengthOffset, workingDepth);

        if (printResult) {
            double[] offXyzFromIdx = refPoints.get(surfacePointIdx.index());
            print(String.format("Calc survey rows deltas %s idx=%s  offn %.2f  offe %.2f ::  offs=-->",
                    approachCorner, surfacePointIdx, offXyzFromIdx[0], offXyzFromIdx[1]));
            System.out.println(String.format("Survey rows deltas offs=%s",
                    refPoints.stream().map(o -> Arrays.toString(o)).collect(Collectors.joining())));
        }

        return refPoints;
    }

    private List<double[]> calcSurveyLinePathOffsetsForRI(double depth, double headingDegs, boolean printResult) {
        double angRads = Math.toRadians(fixHeadingDegsForEntryBottomLeft(headingDegs, approachCorner));
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);

        List<double[]> refPoints = ManeuversUtil.calcRIPatternPoints(targetLength * 1.3  + surveyDeltaAltitudeFromTarget * 10,
                27, 1, 10, true, angRads);
        refPoints.forEach(p -> p[ManeuversUtil.Z] = depthRef);

        refPoints = addApproachAndExitPointsExtendingLines(refPoints, Math.toDegrees(angRads), approachLengthOffset, workingDepth);

        if (printResult) {
            double[] offXyzFromIdx = refPoints.get(surfacePointIdx.index());
            print(String.format("Calc survey ri deltas %s idx=%s  offn %.2f  offe %.2f ::  offs=-->",
                    approachCorner, surfacePointIdx, offXyzFromIdx[0], offXyzFromIdx[1]));
            System.out.println(String.format("Survey ri deltas offs=%s",
                    refPoints.stream().map(o -> Arrays.toString(o)).collect(Collectors.joining())));
        }

        return refPoints;
    }

    private List<double[]> calcSurveyLinePathOffsetsForCrossHatch(double depth, double headingDegs, boolean printResult) {
        double angRads = Math.toRadians(fixHeadingDegsForEntryBottomLeft(headingDegs, approachCorner));
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);

        List<double[]> refPoints = ManeuversUtil.calcCrossHatchPatternPoints(targetLength * 1.3  + surveyDeltaAltitudeFromTarget * 10,
                27, 10, true, angRads);
        refPoints.forEach(p -> p[ManeuversUtil.Z] = depthRef);

        refPoints = addApproachAndExitPointsExtendingLines(refPoints, Math.toDegrees(angRads), approachLengthOffset, workingDepth);

        if (printResult) {
            double[] offXyzFromIdx = refPoints.get(surfacePointIdx.index());
            print(String.format("Calc survey ri deltas %s idx=%s  offn %.2f  offe %.2f ::  offs=-->",
                    approachCorner, surfacePointIdx, offXyzFromIdx[0], offXyzFromIdx[1]));
            System.out.println(String.format("Survey ri deltas offs=%s",
                    refPoints.stream().map(o -> Arrays.toString(o)).collect(Collectors.joining())));
        }

        return refPoints;
    }

    private List<double[]> calcSurveyLinePathOffsetsForExpanding(double depth, double headingDegs, boolean printResult) {
        double angRads = Math.toRadians(headingDegs);
        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);

        boolean invertY;
        switch (approachCorner) {
            case FRONT_LEFT:
                angRads += Math.PI;
                invertY = false;
                break;
            case FRONT_RIGHT:
                angRads += Math.PI;
                invertY = true;
                break;
            case BACK_LEFT:
            default:
                invertY = false;
                break;
            case BACK_RIGHT:
                invertY = true;
                break;
        }

        double expWidth = targetLength * 1.3  + surveyDeltaAltitudeFromTarget * 10;
        List<double[]> refPoints = ManeuversUtil.calcExpansiveSquarePatternPointsMaxBox(
                expWidth, 27, angRads, invertY);
        refPoints.forEach(p -> p[ManeuversUtil.Z] = depthRef);

        refPoints = addApproachAndExitPointsExtendingLines(refPoints, Math.toDegrees(angRads),
                approachLengthOffset + expWidth / 2.0, approachLengthOffset, workingDepth);

        if(printResult) {
            double[] offXyzFromIdx = refPoints.get(surfacePointIdx.index());
            print(String.format("Calc survey expanding deltas %s idx=%s  offn %.2f  offe %.2f ::  offs=-->",
                    approachCorner, surfacePointIdx, offXyzFromIdx[0], offXyzFromIdx[1]));
            System.out.println(String.format("Survey expanding deltas offs=%s",
                    refPoints.stream().map(o -> Arrays.toString(o)).collect(Collectors.joining())));
        }

        return refPoints;
    }

    private double[] calcSurveyLinePoint(double latDegs, double lonDegs, List<double[]> refPoints) {
        double[] ref = refPoints.get(surfacePointIdx.index());
        double offsetX = ref[0];
        double offsetY = ref[1];
        double offsetZ = ref[2];

        double[] pos = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);

        print(String.format("Delta %s %s   offn %.2f  offe %.2f",
                approachCorner, surfacePointIdx,offsetX, offsetY));
        //System.out.println(String.format("Deltas offs=%s",
        //        refPoints.stream().map(o -> Arrays.toString(o)).collect(Collectors.joining())));

        double latDegsRef = pos[0];
        double lonDegsRef = pos[1];
        
        double[] posRef = new double[] { latDegsRef, lonDegsRef, offsetZ };
        return posRef;
    }

    private void resetPatternPathOffsetsForPlan(double refLatDegs, double refLonDegs, double refDepth) {
        patternLatDegsForPlan = refLatDegs;
        patternLonDegsForPlan = refLonDegs;
        patternDepthForPlan = refDepth;
        patternPathOffsetsForPlan.clear();
    }

    private void addOffsetToPatternList(double refLatDegs, double refLonDegs, double refDepth,
            int indexToStartChange, List<double[]> refPointsSublist) {
        // Delete the values still not executed
        patternPathOffsetsForPlan.subList(indexToStartChange, patternPathOffsetsForPlan.size()).clear();
        if (patternLatDegsForPlan == refLatDegs && patternLonDegsForPlan == refLonDegs) {
            patternPathOffsetsForPlan.addAll(refPointsSublist);
        } else {
            List<double[]> nOffsets = refPointsSublist.stream().map(o -> {
                double[] nLLD = WGS84Utilities.WGS84displace(refLatDegs, refLonDegs, refDepth,
                        o[0], o[1], o[2]);
                double[] nNED = WGS84Utilities.WGS84displacement(patternLatDegsForPlan, patternLonDegsForPlan,
                        patternDepthForPlan, nLLD[0], nLLD[1], nLLD[2]);
                return nNED;
            }).collect(Collectors.toList());
            patternPathOffsetsForPlan.addAll(nOffsets);
        }
    }

    private <M extends Maneuver> void sendPlanToVehicleDb(String planName, M... maneuvers) {
        try {
            PlanDB msg = PlanPointsUtil.createPlanDBAdd(PlanPointsUtil
                    .createPlanSpecification(planCreationPrefix + planName, maneuvers));
            if (msg != null)
                send(msg);
        }
        catch (IOException e) {
            printError(e.getMessage());
        }
    }

    private int calcRefPointsHashCode(List<double[]> refPoints) {
        return Arrays.hashCode(refPoints.stream().map(e -> Arrays.hashCode(e)).toArray());
    }

    private void markState(FSMState state) {
        stateToReturnTo = state;
    }

    private boolean isGoSurfaceTime() {
        return usePeriodicSurfaceForPos && curTimeMillis - atSurfaceMillis > minsUnderwater * 60 * 1E3;
    }

    public FSMState loiterUnderwaterState(FollowRefState ref) {
        printFSMState();
        markState(this::loiterUnderwaterState);
        
        double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
        setLoiterRef(loiterPos[0], loiterPos[1], workingDepth);
        setCourseSpeed();
        int pHash = Arrays.hashCode(loiterPos);
        if (lastSentPlanRefHash != pHash) {
            sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(loiterPos[0], loiterPos[1],
                    workingDepth, getCourseSpeedValue(), getCourseSpeedUnit()));
            lastSentPlanRefHash = pHash;
        }

        switch (surveyStage) {
            case ON_GOING:
                break;
            case WAITING_TO_START:
            default:
                if (keepInParkingPosAtStart && (Double.isFinite(latDegParking) && Double.isFinite(lonDegParking))) {
                    print(String.format("Ref to parking PLat %.6f    PLon %.6f", latDegParking, lonDegParking));
                    setLoiterRef(latDegParking, lonDegParking, workingDepth);
                    pHash = Arrays.hashCode(new double[]{latDegParking, lonDegParking, workingDepth});
                    if (lastSentPlanRefHash != pHash) {
                        sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(latDegParking, lonDegParking,
                                workingDepth, getCourseSpeedValue(), getCourseSpeedUnit()));
                        lastSentPlanRefHash = pHash;
                    }
                }
                break;
        }

        if (!isUnderwater())
            atWaitingUnderwaterMillis = -1;

        return this::loiterUnderwaterStayState;
    }

    public FSMState loiterUnderwaterStayState(FollowRefState ref) {
    	printFSMState();
        markState(this::loiterUnderwaterState);

        if (!isUnderwater()) {
            atWaitingUnderwaterMillis = -1;
            return this::loiterUnderwaterStayState;
        }

        if (atWaitingUnderwaterMillis == -1) {
            atWaitingUnderwaterMillis = curTimeMillis;
            double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
            setLoiterRef(loiterPos[0], loiterPos[1], workingDepth);
            int pHash = Arrays.hashCode(loiterPos);
            if (lastSentPlanRefHash != pHash) {
                sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(loiterPos[0], loiterPos[1],
                        workingDepth, getCourseSpeedValue(), getCourseSpeedUnit()));
                lastSentPlanRefHash = pHash;
            }

            switch (surveyStage) {
                case ON_GOING:
                    break;
                case WAITING_TO_START:
                default:
                    if (keepInParkingPosAtStart && (Double.isFinite(latDegParking) && Double.isFinite(lonDegParking))) {
                        print(String.format("Ref to parking PLat %.6f    PLon %.6f", latDegParking, lonDegParking));
                        setLoiterRef(latDegParking, lonDegParking, workingDepth);
                        pHash = Arrays.hashCode(new double[]{latDegParking, lonDegParking, workingDepth});
                        if (lastSentPlanRefHash != pHash) {
                            sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(latDegParking, lonDegParking,
                                    workingDepth, getCourseSpeedValue(), getCourseSpeedUnit()));
                            lastSentPlanRefHash = pHash;
                        }
                    }
                    break;
            }
        }

        if (isDistressKnownToStart()) {
            if (isGoSurfaceTime()) {
                markState(this::approachSurveyPointState);
                goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
                return this::goSurfaceState;
            }
            else {
                return this::approachSurveyPointState;
            }
        }
        else {
            if (curTimeMillis - atWaitingUnderwaterMillis > waitDistressUnderwaterPeriodicResurfaceMinutes * 60 * 1E3) {
                goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
                return this::goSurfaceState;
            }
        }

        return this::loiterUnderwaterStayState;
    }

    public FSMState goSurfaceState(FollowRefState ref) {
    	printFSMState();
        // markState(this::goSurfaceState);
        
        double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
        setSurfaceLoiterRef(loiterPos[0], loiterPos[1]);
        setCourseSpeed();
        int pHash = Arrays.hashCode(loiterPos);
        if (lastSentPlanRefHash != pHash) {
            sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(loiterPos[0], loiterPos[1],
                    0, getCourseSpeedValue(), getCourseSpeedUnit()));
            lastSentPlanRefHash = pHash;
        }

        // if (isUnderwater())
            atSurfaceMillis = -1;

        switch (goSurfaceTask) {
            case PREEMPTIVE_OP:
                break;
            case END_OP:
                break;
            case START_OP:
            default:
                break;
        }

        return this::goSurfaceStayState;
    }

    public FSMState goSurfaceStayState(FollowRefState ref) {
    	printFSMState();
        // markState(this::goSurfaceState);

        if (isUnderwater()) {
            print("Waiting to surface");
            atSurfaceMillis = -1;
            return this::goSurfaceStayState;
        }
        else if (hasGps()) {
            if (!Double.isFinite(latDegParking) || !Double.isFinite(lonDegParking)) {
                double[] curPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
                latDegParking = curPos[0];
                lonDegParking = curPos[1];
                print(String.format("Set PLat %.6f    PLon %.6f", latDegParking, lonDegParking));
            }
        }

        if (atSurfaceMillis == -1) {
            atSurfaceMillis = curTimeMillis;
            double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
            setSurfaceLoiterRef(loiterPos[0], loiterPos[1]);
            int pHash = Arrays.hashCode(loiterPos);
            if (lastSentPlanRefHash != pHash) {
                sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(loiterPos[0], loiterPos[1],
                        0, getCourseSpeedValue(), getCourseSpeedUnit()));
                lastSentPlanRefHash = pHash;
            }
        }
        
        switch (surveyStage) {
            case ON_GOING:
                break;
            case WAITING_TO_START:
            default:
                if (keepInParkingPosAtStart && (Double.isFinite(latDegParking) && Double.isFinite(lonDegParking))) {
                    print(String.format("Ref to parking PLat %.6f    PLon %.6f", latDegParking, lonDegParking));
                    setSurfaceLoiterRef(latDegParking, lonDegParking);
                    int pHash = Arrays.hashCode(new double[]{latDegParking, lonDegParking});
                    if (lastSentPlanRefHash != pHash) {
                        sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(latDegParking, lonDegParking,
                                0, getCourseSpeedValue(), getCourseSpeedUnit()));
                        lastSentPlanRefHash = pHash;
                    }
                }
                break;
        }
        
        switch (goSurfaceTask) {
            case PREEMPTIVE_OP:
                if (hasGps()) {
                    sendReportMsg();
                    return stateToReturnTo;
                }
                break;
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
    	printFSMState();
        markState(this::approachSurveyPointState);
        
        surveyStage = SurveyStageEnum.ON_GOING;
        
        if (isGoSurfaceTime()) {
            goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
            return this::goSurfaceState;
        }

        DistressPosition dp = AisCsvParser.distressPosition;

        approachCorner = calcApproachCorner(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        List<double[]> refPoints = calcSurveyLinePathOffsets(dp.depth, dp.headingDegs);
        double[] posRef = calcApproachPoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, refPoints);

        setGoingRef(posRef[0], posRef[1], posRef[2]);
        setCourseSpeed();
        int pHash = Arrays.hashCode(posRef);
        if (lastSentPlanRefHash != pHash) {
            sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(posRef[0], posRef[1],
                    posRef[2], getCourseSpeedValue(), getCourseSpeedUnit()));
            lastSentPlanRefHash = pHash;
        }

        resetPatternPathOffsetsForPlan(dp.latDegs, dp.lonDegs, 0);
        addOffsetToPatternList(dp.latDegs, dp.lonDegs, 0, surfacePointIdx.index(),
                surfacePointIdx.index() == 0 ? refPoints : refPoints.subList(surfacePointIdx.index(), refPoints.size()));
        pHash = calcRefPointsHashCode(refPoints);
        if (lastSentPlanPatternHash != pHash) {
            sendPlanToVehicleDb(PLAN_PATTERN_SUFFIX, PlanPointsUtil.createFollowPathFrom(patternLatDegsForPlan,
                    patternLonDegsForPlan, patternDepthForPlan, getCourseSpeedValue(), getCourseSpeedUnit(),
                    patternPathOffsetsForPlan, ""));
            lastSentPlanPatternHash = pHash;
        }

        return this::approachSurveyPointStayState;
    }

    public FSMState approachSurveyPointStayState(FollowRefState ref) {
    	printFSMState();
        markState(this::approachSurveyPointState);

        if (isGoSurfaceTime()) {
            goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
            return this::goSurfaceState;
        }

        DistressPosition dp = AisCsvParser.distressPosition;
        List<double[]> refPoints = calcSurveyLinePathOffsets(dp.depth, dp.headingDegs);
        double[] newPosRef = calcApproachPoint(dp.latDegs, dp.lonDegs, dp.depth, dp.headingDegs, refPoints);
        double distToNewRef = WGS84Utilities.distance(Math.toDegrees(ref.reference.lat),
                Math.toDegrees(ref.reference.lon), newPosRef[0], newPosRef[1]);
        EstimatedState estState = get(EstimatedState.class);
        double[] curPos = WGS84Utilities.toLatLonDepth(estState);
        @SuppressWarnings("unused")
        double distToRef = WGS84Utilities.distance(curPos[0], curPos[1], newPosRef[0], newPosRef[1]);
        
        if (distToNewRef > deltaDistToAdjustApproach) {
            setGoingRef(newPosRef[0], newPosRef[1], newPosRef[2]);
            int pHash = Arrays.hashCode(newPosRef);
            if (lastSentPlanRefHash != pHash) {
                sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(newPosRef[0], newPosRef[1],
                        newPosRef[2], getCourseSpeedValue(), getCourseSpeedUnit()));
                lastSentPlanRefHash = pHash;
            }

            addOffsetToPatternList(dp.latDegs, dp.lonDegs, 0, surfacePointIdx.index(),
                    surfacePointIdx.index() == 0 ? refPoints : refPoints.subList(surfacePointIdx.index(), refPoints.size()));
            pHash = calcRefPointsHashCode(refPoints);
            if (lastSentPlanPatternHash != pHash) {
                sendPlanToVehicleDb(PLAN_PATTERN_SUFFIX, PlanPointsUtil.createFollowPathFrom(patternLatDegsForPlan,
                        patternLonDegsForPlan, patternDepthForPlan, getCourseSpeedValue(), getCourseSpeedUnit(),
                        patternPathOffsetsForPlan, ""));
                lastSentPlanPatternHash = pHash;
            }

            return this::approachSurveyPointStayState;
        }
        
        if (arrivedXY()) {
            return this::firstSurveyPointState;
        }
            
        return this::approachSurveyPointStayState;
    }

    public FSMState firstSurveyPointState(FollowRefState ref) {
    	printFSMState();
        markState(this::firstSurveyPointState);

        boolean permitResurface = surfacePointIdx.test(idx -> {
            Function<Integer, Boolean> testPermitSurface;
            switch (surveyPattern) {
                case DEFAULT: // index approach(0) or 2;
                    testPermitSurface = p -> (p & 1) == 0;
                    break;
                case ROWS: // index approach(0) and corners (odd) except start(0);
                case RI:
                case CROSS:
                    testPermitSurface = p -> p != 1 && ((p & 1) == 1);
                    break;
                case EXPANDING: // approach(0) or last
                    testPermitSurface = p -> p == 0 || p == surfacePointIdx.getMax();
                    break;
                default:
                    testPermitSurface = p -> false;
                    break;
            }
            return testPermitSurface.apply(surfacePointIdx.index());
        });

        if (surfaceOnCorners && permitResurface) {
            if (isGoSurfaceTime()) {
                goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
                deactivatePayload();
                return this::goSurfaceState;
            }
        }
        
        surfacePointIdx.incrementAndGetIndex();

//        surfacePointIdx.test(idx -> {
//            switch (idx) {
//                case 1:
//                case 3:
//                    activatePayload();
//                    break;
//                case 0:
//                case 2:
//                    if (!usePeriodicSurfaceForPos) {
//                        activatePayload();
//                        break;
//                    }
//                default:
//                    deactivatePayload();
//                    break;
//            }
//            return true;
//        });
        if (!permitResurface) {
            activatePayload();
        } else {
            if (!usePeriodicSurfaceForPos) {
                activatePayload();
            }
        }

        DistressPosition dp = AisCsvParser.distressPosition;
        List<double[]> refPoints = calcSurveyLinePathOffsets(dp.depth, dp.headingDegs);
        double[] posRef = calcSurveyLinePoint(dp.latDegs, dp.lonDegs, refPoints);
        setGoingRef(posRef[0], posRef[1], posRef[2]);
        setSurveySpeed();
        int pHash = Arrays.hashCode(posRef);
        if (lastSentPlanRefHash != pHash) {
            sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(posRef[0], posRef[1],
                    posRef[2], getSurveySpeedValue(), getSurveySpeedUnit()));
            lastSentPlanRefHash = pHash;
        }

        addOffsetToPatternList(dp.latDegs, dp.lonDegs, 0, surfacePointIdx.index(),
                surfacePointIdx.index() == 0 ? refPoints : refPoints.subList(surfacePointIdx.index(), refPoints.size()));

        pHash = calcRefPointsHashCode(refPoints);
        if (lastSentPlanPatternHash != pHash) {
            sendPlanToVehicleDb(PLAN_PATTERN_SUFFIX, PlanPointsUtil.createFollowPathFrom(patternLatDegsForPlan,
                    patternLonDegsForPlan, patternDepthForPlan, getCourseSpeedValue(), getCourseSpeedUnit(),
                    patternPathOffsetsForPlan, ""));
            lastSentPlanPatternHash = pHash;
        }

        return this::firstSurveyPointStayState;
    }

    public FSMState firstSurveyPointStayState(FollowRefState ref) {
    	printFSMState();
        markState(this::firstSurveyPointState);

        DistressPosition dp = AisCsvParser.distressPosition;
        List<double[]> refPoints = calcSurveyLinePathOffsets(dp.depth, dp.headingDegs);
        double[] newPosRef = calcSurveyLinePoint(dp.latDegs, dp.lonDegs, refPoints);
        double distToNewRef = WGS84Utilities.distance(Math.toDegrees(ref.reference.lat),
                Math.toDegrees(ref.reference.lon), newPosRef[0], newPosRef[1]);
        EstimatedState estState = get(EstimatedState.class);
        double[] curPos = WGS84Utilities.toLatLonDepth(estState);
        @SuppressWarnings("unused")
        double distToRef = WGS84Utilities.distance(curPos[0], curPos[1], newPosRef[0], newPosRef[1]);

        if (distToNewRef > deltaDistToAdjustApproach) {
            setGoingRef(newPosRef[0], newPosRef[1], newPosRef[2]);
            int pHash = Arrays.hashCode(newPosRef);
            if (lastSentPlanRefHash != pHash) {
                sendPlanToVehicleDb(PLAN_REF_SUFFIX, PlanPointsUtil.createGotoFrom(newPosRef[0], newPosRef[1],
                        newPosRef[2], getSurveySpeedValue(), getSurveySpeedUnit()));
                lastSentPlanRefHash = pHash;
            }

            addOffsetToPatternList(dp.latDegs, dp.lonDegs, 0, surfacePointIdx.index(),
                    surfacePointIdx.index() == 0 ? refPoints : refPoints.subList(surfacePointIdx.index(), refPoints.size()));

            pHash = calcRefPointsHashCode(refPoints);
            if (lastSentPlanPatternHash != pHash) {
                sendPlanToVehicleDb(PLAN_PATTERN_SUFFIX, PlanPointsUtil.createFollowPathFrom(patternLatDegsForPlan,
                        patternLonDegsForPlan, patternDepthForPlan, getCourseSpeedValue(), getCourseSpeedUnit(),
                        patternPathOffsetsForPlan, ""));
                lastSentPlanPatternHash = pHash;
            }

            return this::firstSurveyPointStayState;
        }
        
        if (arrivedXY()) {
            if (surveySideOrAround && surveyPattern == SurveyPatternEnum.DEFAULT
                    || surfacePointIdx.isMaxIndex()) {
                goSurfaceTask = GoSurfaceTaskEnum.END_OP;
                deactivatePayload();
                return this::goSurfaceState;
            }
            else {
                return this::firstSurveyPointState;
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

    @Periodic(value = 90000)
    private void sendDistressTest() {
        if (!testTargetSimulate)
            return;
        
        if (tmpTargetLastSendTime <= 0) {
            tmpTargetLat = testTargetLat;
            tmpTargetLon = testTargetLon;
            tmpTargetDepth = testTargetDepth;
            tmpTargetHeading = testTargetHeading;
            tmpTargetSpeedKt = testTargetSpeedKt;
        }
        else {
            long deltaTMillis = System.currentTimeMillis() - tmpTargetLastSendTime;
            double deltaTMeters = tmpTargetSpeedKt / MS_TO_KNOT * deltaTMillis / 1E3;
            double angRads = Math.toRadians(tmpTargetHeading);
            double offsetX = Math.cos(angRads) * deltaTMeters;
            double offsetY = Math.sin(angRads) * deltaTMeters;
            double[] pos = WGS84Utilities.WGS84displace(tmpTargetLat, tmpTargetLon, 0, offsetX, offsetY, 0);
            tmpTargetLat = pos[0];
            tmpTargetLon = pos[1];
            tmpTargetHeading += tmpTargetRandom.nextGaussian() * testTargetHeadingGaussianNoiseDegs;
        }
        
        String dis = getAisDistressString(tmpTargetLat, tmpTargetLon, tmpTargetDepth, tmpTargetSpeedKt, tmpTargetHeading);
        parseAISTxtSentence(dis + "\r\n");
        
        // 263029000,NRP Arpao
        // 263125000,NRP Hidra
        String aisPos = getAisPositionString("NRP Arpao", "Submarine", tmpTargetLat, tmpTargetLon, tmpTargetDepth, tmpTargetSpeedKt, tmpTargetHeading,
                tmpTargetHeading, 0, "0", System.currentTimeMillis());
        byte[] buf = aisPos.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(aisHostAddr), aisUdpHostPort);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.send(packet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        tmpTargetLastSendTime = System.currentTimeMillis();
    }

    @Override
    public void connect() throws Exception {
        print("Distress Survey started");

        StringBuilder sb = new StringBuilder();
        sb.append("Distress Survey read the following settings:");
        for (Field f : getClass().getDeclaredFields()) {
            Parameter p = f.getAnnotation(Parameter.class);
            if (p != null) {
                sb.append("\n").append(f.getName()).append("=").append(f.get(this));
            }
        }
        System.out.println(sb.append("\n").toString());

        init();
        setPaused(false);
        connect(hostAddr, hostPort);
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

        System.out.println("Distress Survey starting...");

        Properties props = new Properties();
        props.load(new FileInputStream(file));
                
        DistressSurvey tracker = PojoConfig.create(DistressSurvey.class, props);

        tracker.connect();
        tracker.join();
    }
}
