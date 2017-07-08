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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import pt.lsts.imc4j.msg.Announce;
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
        END_OP,
        PREEMPTIVE_OP
    }

    private enum SurveyPathEnum {
        FIRST,
        SECOND,
        THIRD,
        FORTH
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

    @Parameter(description = "DUNE Host Address")
    private String hostAddr = "127.0.0.1";
    @Parameter(description = "DUNE Host Port (TCP)")
    private int hostPort = 6003;
    @Parameter(description = "DUNE plan to execute right after termination")
    private String endPlanToUse = "rendezvous";
    
    @Parameter(description = "Minutes before termination")
    private int minutesTimeout = 60;
    
    @Parameter(description = "Maximum time underwater (minutes)")
    private int minsUnderwater = 15;
    @Parameter(description = "Surface on Corners")
    private boolean usePeriodicSurfaceForPos = false;
    @Parameter(description = "Surface on Corners")
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
    @Parameter(description = "Approach Lenght Offset")
    private double approachLenghtOffset = 50;
    @Parameter(description = "Survey Side (true) or Around (false)")
    private boolean surveySideOrAround = false;

    @Parameter(description = "Target Width")
    private double targetWidth = 6.3;
    @Parameter(description = "Target Lenght")
    private double targetLenght = 65;

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
    private SurveyPathEnum surfacePointIdx = SurveyPathEnum.FIRST;
    private SurveyStageEnum surveyStage = SurveyStageEnum.WAITING_TO_START;

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
            System.out.println("Will terminate by " + deadline + " and execute '" + endPlanToUse + "'");
        }
        else
            System.out.println("Will terminate by " + deadline);

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

    
    @Periodic(value = 1000)
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
    
    private void setSurveySpeed() {
        setCourseSpeed();
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
    
    private ApproachCornerEnum calcApproachCorner(double latDegs, double lonDegs, double depth, double headingDegs, double speedKnots,
            long timestamp) {
        double[] vehPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
        ApproachCornerEnum approachCorner = ApproachCornerEnum.FRONT_LEFT;
        double angRads = Math.toRadians(headingDegs);
        double offsetX = Math.cos(angRads) * targetLenght / 2.;
        double offsetY = Math.sin(angRads) * targetLenght / 2.;
        double[] frontCenterPoint = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);
        offsetX = Math.cos(angRads) * -targetLenght / 2.;
        offsetY = Math.sin(angRads) * -targetLenght / 2.;
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
    
    private double[] calcApproachPoint(double latDegs, double lonDegs, double depth, double headingDegs, double speedKnots,
            long timestamp) {
        surfacePointIdx = SurveyPathEnum.FIRST;
        return calcSurveyLinePoint(latDegs, lonDegs, depth, headingDegs, speedKnots, timestamp);
    }

    private double[] calcSurveyLinePoint(double latDegs, double lonDegs, double depth, double headingDegs,
            double speedKnots, long timestamp) {
        double angRads = Math.toRadians(headingDegs);
        double ol = -targetLenght;
        double ow = -(targetWidth + surveyDeltaAltitudeFromTarget * 10);

        double depthRef = Math.max(0, depth - surveyDeltaAltitudeFromTarget);

        ArrayList<Double> olPointsList = new ArrayList<>();
        ArrayList<Double> owPointsList = new ArrayList<>();
        // FrontLeft
        olPointsList.add(targetLenght); // + approachLenghtOffset;
        owPointsList.add(-(targetWidth + surveyDeltaAltitudeFromTarget * 10));
        // BackLeft
        olPointsList.add(-targetLenght);
        owPointsList.add(-(targetWidth + surveyDeltaAltitudeFromTarget * 10));
        // BackRight
        olPointsList.add(-targetLenght);
        owPointsList.add(targetWidth + surveyDeltaAltitudeFromTarget * 10);
        // FrontRight
        olPointsList.add(targetLenght); // + approachLenghtOffset;
        owPointsList.add(targetWidth + surveyDeltaAltitudeFromTarget * 10);
        
        double approachLenghtOffsetFixed = approachLenghtOffset;
        
        switch (approachCorner) {
            case FRONT_LEFT: // 1,2,3,4
            default:
                break;
            case BACK_LEFT: // 2,1,4,3
                Collections.reverse(olPointsList);
                Collections.reverse(owPointsList);
                Collections.rotate(olPointsList, 2);
                Collections.rotate(owPointsList, 2);
                approachLenghtOffsetFixed *= -1;
                break;
            case BACK_RIGHT: // 3,4,1,2
                Collections.rotate(olPointsList, 2);
                Collections.rotate(owPointsList, 2);
                approachLenghtOffsetFixed *= -1;
                break;
            case FRONT_RIGHT: // 4,3,2,1
                Collections.reverse(olPointsList);
                Collections.reverse(owPointsList);
                break;
        }
        
        switch (surfacePointIdx) {
            case FIRST:
            default:
                ol = olPointsList.get(0) + approachLenghtOffsetFixed;
                ow = owPointsList.get(0);
                depthRef = workingDepth;
                break;
            case SECOND:
                ol = olPointsList.get(1);
                ow = owPointsList.get(1);
                break;
            case THIRD:
                ol = olPointsList.get(2);
                ow = owPointsList.get(2);
                break;
            case FORTH:
                ol = olPointsList.get(3) + approachLenghtOffsetFixed;
                ow = owPointsList.get(3);
                break;
        }
        
        double offsetX = Math.cos(angRads) * ol - Math.sin(angRads) * ow;
        double offsetY = Math.sin(angRads) * ol + Math.cos(angRads) * ow;
        double[] pos = WGS84Utilities.WGS84displace(latDegs, lonDegs, 0, offsetX, offsetY, 0);

        print(String.format("Delta %s %s   l %.2f  w %.2f  offn %.2f  offe %.2f ::  %s  %s", approachCorner,
                surfacePointIdx, ol, ow, offsetX, offsetY, Arrays.toString(olPointsList.toArray()),
                Arrays.toString(owPointsList.toArray())));

        double latDegsRef = pos[0];
        double lonDegsRef = pos[1];
        
        double[] posRef = new double[] { latDegsRef, lonDegsRef, depthRef };
        return posRef;
    }

    private void markState(FSMState state) {
        stateToReturnTo = state;
    }

    private boolean isGoSurfaceTime() {
        return usePeriodicSurfaceForPos && curTimeMillis - atSurfaceMillis > minsUnderwater * 60 * 1E3;
    }

    public FSMState loiterUnderwaterState(FollowRefState ref) {
        printFSMStateName("loiterUnderwaterState");
        markState(this::loiterUnderwaterState);
        
        double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
        setLoiterRef(loiterPos[0], loiterPos[1], workingDepth);
        setCourseSpeed();

        switch (surveyStage) {
            case ON_GOING:
                break;
            case WAITING_TO_START:
            default:
                if (keepInParkingPosAtStart && (Double.isFinite(latDegParking) && Double.isFinite(lonDegParking))) {
                    print(String.format("Ref to parking PLat %.6f    PLon %.6f", latDegParking, lonDegParking));
                    setLoiterRef(latDegParking, lonDegParking, workingDepth);
                }
                break;
        }

        if (!isUnderwater())
            atWaitingUnderwaterMillis = -1;

        return this::loiterUnderwaterStayState;
    }

    public FSMState loiterUnderwaterStayState(FollowRefState ref) {
        printFSMStateName("loiterUnderwaterStayState");
        markState(this::loiterUnderwaterState);

        if (!isUnderwater()) {
            atWaitingUnderwaterMillis = -1;
            return this::loiterUnderwaterStayState;
        }

        if (atWaitingUnderwaterMillis == -1) {
            atWaitingUnderwaterMillis = curTimeMillis;
            double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
            setLoiterRef(loiterPos[0], loiterPos[1], workingDepth);
            
            switch (surveyStage) {
                case ON_GOING:
                    break;
                case WAITING_TO_START:
                default:
                    if (keepInParkingPosAtStart && (Double.isFinite(latDegParking) && Double.isFinite(lonDegParking))) {
                        print(String.format("Ref to parking PLat %.6f    PLon %.6f", latDegParking, lonDegParking));
                        setLoiterRef(latDegParking, lonDegParking, workingDepth);
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
        printFSMStateName("goSurfaceState");
        // markState(this::goSurfaceState);
        
        double[] loiterPos = WGS84Utilities.toLatLonDepth(get(EstimatedState.class));
        setSurfaceLoiterRef(loiterPos[0], loiterPos[1]);
        setCourseSpeed();

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
        printFSMStateName("goSurfaceStayState");
        // markState(this::goSurfaceState);

        if (isUnderwater()) {
            print("Wainting to surface");
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
        }
        
        switch (surveyStage) {
            case ON_GOING:
                break;
            case WAITING_TO_START:
            default:
                if (keepInParkingPosAtStart && (Double.isFinite(latDegParking) && Double.isFinite(lonDegParking))) {
                    print(String.format("Ref to parking PLat %.6f    PLon %.6f", latDegParking, lonDegParking));
                    setSurfaceLoiterRef(latDegParking, lonDegParking);
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
        printFSMStateName("approachSurveyPointState");
        markState(this::approachSurveyPointState);
        
        surveyStage = SurveyStageEnum.ON_GOING;
        
        if (isGoSurfaceTime()) {
            goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
            return this::goSurfaceState;
        }

        DistressPosition dp = AisCsvParser.distressPosition;

        approachCorner = calcApproachCorner(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        double[] posRef = calcApproachPoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        
        setGoingRef(posRef[0], posRef[1], posRef[2]);
        setCourseSpeed();
        
        return this::approachSurveyPointStayState;
    }

    public FSMState approachSurveyPointStayState(FollowRefState ref) {
        printFSMStateName("approachSurveyPointStayState");
        markState(this::approachSurveyPointState);

        if (isGoSurfaceTime()) {
            goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
            return this::goSurfaceState;
        }

        DistressPosition dp = AisCsvParser.distressPosition;
        double[] newPosRef = calcApproachPoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        double distToNewRef = WGS84Utilities.distance(Math.toDegrees(ref.reference.lat), Math.toDegrees(ref.reference.lon), newPosRef[0], newPosRef[1]);
        EstimatedState estState = get(EstimatedState.class);
        double[] curPos = WGS84Utilities.toLatLonDepth(estState);
        @SuppressWarnings("unused")
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
        markState(this::firstSurveyPointState);

        if (surfaceOnCorners && (SurveyPathEnum.FIRST.compareTo(surfacePointIdx) == 0
                || SurveyPathEnum.THIRD.compareTo(surfacePointIdx) == 0)) {
            if (isGoSurfaceTime()) {
                goSurfaceTask = GoSurfaceTaskEnum.PREEMPTIVE_OP;
                return this::goSurfaceState;
            }
        }
        
        surfacePointIdx = SurveyPathEnum.values()[surfacePointIdx.ordinal() + 1];
        DistressPosition dp = AisCsvParser.distressPosition;
        double[] posRef = calcSurveyLinePoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        setGoingRef(posRef[0], posRef[1], posRef[2]);
        setSurveySpeed();
        
        return this::firstSurveyPointStayState;
    }

    public FSMState firstSurveyPointStayState(FollowRefState ref) {
        printFSMStateName("firstSurveyPointStayState");
        markState(this::firstSurveyPointState);

        DistressPosition dp = AisCsvParser.distressPosition;
        double[] newPosRef = calcSurveyLinePoint(dp.latDegs, dp.lonDegs, dp.depth , dp.headingDegs, dp.speedKnots, dp.timestamp);
        double distToNewRef = WGS84Utilities.distance(Math.toDegrees(ref.reference.lat), Math.toDegrees(ref.reference.lon), newPosRef[0], newPosRef[1]);
        EstimatedState estState = get(EstimatedState.class);
        double[] curPos = WGS84Utilities.toLatLonDepth(estState);
        @SuppressWarnings("unused")
        double distToRef = WGS84Utilities.distance(curPos[0], curPos[1], newPosRef[0], newPosRef[1]);

        if (distToNewRef > deltaDistToAdjustApproach) {
            setGoingRef(newPosRef[0], newPosRef[1], newPosRef[2]);
            return this::firstSurveyPointStayState;
        }
        
        if (arrivedXY()) {
            if (surveySideOrAround || surfacePointIdx.ordinal() == SurveyPathEnum.values().length - 1) {
                goSurfaceTask = GoSurfaceTaskEnum.END_OP; 
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
