/**
 * 
 */
package pt.lsts.backseat.distress;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import pt.lsts.backseat.TimedFSM;
import pt.lsts.backseat.distress.ais.AisCsvParser;
import pt.lsts.backseat.distress.net.TCPConnection;
import pt.lsts.backseat.distress.net.UDPConnection;
import pt.lsts.imc4j.annotations.Parameter;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.FollowRefState;
import pt.lsts.imc4j.util.AngleUtils;
import pt.lsts.imc4j.util.PojoConfig;
import pt.lsts.imc4j.util.WGS84Utilities;

/**
 * @author pdias
 *
 */
public class DistressSurvey extends TimedFSM {

    public static final double MS_TO_KNOT = 3.6 / 1.852; // 1.9438444924406047516198704103672
    
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

    private TCPConnection aisTxtTcp = null;
    private UDPConnection aisTxtUdp = null;
    
    public DistressSurvey() {
        state = this::waitState;
    }

    public void init() {
        deadline = new Date(System.currentTimeMillis() + minutesTimeout * 60 * 1000);
        if (!endPlanToUse.isEmpty()) {
            endPlan = endPlanToUse;
            System.out.println("Will terminate by " + deadline + " and execute '" + endPlanToUse + "'");
        }
        else
            System.out.println("Will terminate by "+deadline);
        
        aisTxtTcp = new TCPConnection(aisHostAddr, aisHostPort);
        aisTxtTcp.register(this::parseAISTxtSentence);
        aisTxtTcp.connect();
        
        aisTxtUdp = new UDPConnection(aisUdpHostPort);
        aisTxtUdp.register(this::parseAISTxtSentence);
        aisTxtUdp.connect();
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
        double depth = pos[2];
        double speedKt = Math.sqrt(es.vx * es.vx + es.vy * es.vy + es.vz * es.vz) * MS_TO_KNOT;
        double headingDeg = AngleUtils.nomalizeAngleDegrees360(Math.toDegrees(es.psi));
        double courseDeg = AngleUtils.nomalizeAngleDegrees180(Math.toDegrees(AngleUtils.calcAngle(0, 0, es.vy, es.vx)));
        double rateOfTurn = AngleUtils.nomalizeAngleDegrees180(Math.toDegrees(es.r));
        String navStatus = "0";// "n.a";
        double timeStampSecs = System.currentTimeMillis() / 1E3;
        // "AIS,Node_Name=000000001,Node_Type=AUV,Latitude=43.603935,Longitude=9.0797591,Depth=0,Speed=22.8,Heading=0,Course=0,RateOfTurn=n.a.,Navigation_Status=n.a.,Timestamp=1498496471.09482,Number_Contacts=0";
        String aisTxt = String.format(Locale.ENGLISH, "AIS,Node_Name=%d,Node_Type=%s,Latitude=%.8f,Longitude=%.8f,Depth=%.1f,Speed=%.1f,"
                + "Heading=%.0f,Course=%.0f,RateOfTurn=%.1f,Navigation_Status=%s,Timestamp=%f,Number_Contacts=0",
                mmsid , type, latDeg, lonDeg, depth, speedKt, headingDeg, courseDeg, rateOfTurn, navStatus,
                timeStampSecs);
        System.out.println((aisTxtTcp.send(aisTxt + "\r\n") ? "Sent" : "Not able to send") + " AIS txt pos. message: " + aisTxt);
    }
    
    /**
     * State Machine State
     */
    public FSMState waitState(FollowRefState ref) {
        return this::waitState;
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
