package pt.lsts.backseat.distress.ais;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.util.AngleUtils;
import pt.lsts.imc4j.util.PeriodicCallbacks;

/**
 * @author pdias
 *
 */
public class AisCsvParser {

    private static File cache = new File("ais.cache");
    private static LinkedHashMap<Integer, String> labelCache = new LinkedHashMap<>();

    public static class DistressPosition {
        public String nation = "n.a";
        public double latDegs = 0;
        public double lonDegs = 0;
        public double depth = 0;
        public double speedKnots = 0;
        public double headingDegs = 0;
        public long timestamp = -1;
    }

    public static class DistressStatus {
        public String nation = "n.a";
        public double o2Percentage = 0;
        public double co2Percentage = 0;
        public double coPpm = 0;
        public double h2Percentage = 0;
        public double presureAtm = 0;
        public double temperatureDegCentigrade = 0;
        public int survivors = 0;
        public long timestamp = -1;
    }

    public static class AISContact {
        public int mmsid = 0;
        public String name = null;
        public String type = "";
        public double latDegs = 0;
        public double lonDegs = 0;
        public double depth = 0;
        public double speedKnots = 0;
        public double headingDegs = 0;
        public double courseDegs = 0;
        public double rateOfTurnDegsPerMin = Double.NaN;
        public double timestamp = Double.NaN;
        public int navStatus = -1;
    }
    
    public static Map<Integer, AISContact> aisDB = Collections.synchronizedMap(new HashMap<Integer, AISContact>());
    
    public static DistressPosition distressPosition = null;
    public static DistressStatus distressStatus = null;

    static {
        if (cache.canRead()) {
            int count = 0;
            try {
                BufferedReader reader = new BufferedReader(new FileReader(cache));
                String line = reader.readLine();
                while (line != null) {
                    String[] parts = line.split(",");
                    int mmsi;
                    try {
                        mmsi = Integer.parseInt(parts[0]);
                    }
                    catch (Exception e1) {
                        mmsi = Integer.parseInt(parts[0].replaceAll("^0x", ""), 16);
                    }
                    String name = parts[1].trim();
                    labelCache.put(mmsi, name);
                    
                    HashMap<String, Object> dimV = new HashMap<>();
                    for (int i = 2; i < parts.length; i++) {
                        String tk = parts[i].trim();
                        String[] prs = tk.split("=");
                        if (prs.length > 1) {
                            String n = prs[0].trim();
                            try {
                                double v = Double.parseDouble(prs[1].trim());
                                dimV.put(n, v);
                            }
                            catch (Exception e) {
                                System.out.println(String.format("Not found a number, adding as string for %s", n));
                                dimV.put(n, prs[1].trim());
                            }
                        }
                    }
//                if (dimV.size() > 0)
//                    dimensionsCache.put(mmsi, dimV);
                    
                    line = reader.readLine();
                    count++;
                }
                reader.close();
                System.out.println("Read " + count + " vessel names from " + cache.getAbsolutePath());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        PeriodicCallbacks.register(new AisCsvParser());
    }
    
    private AisCsvParser() {
    }

    @Periodic(value = 10000)
    public static void saveCache() {
        int count = 0;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(cache));

            for (Entry<Integer, String> entry : labelCache.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(entry.getKey()).append(",").append(entry.getValue());

//                HashMap<String, Object> dimV = dimensionsCache.get(entry.getKey());
//                if (dimV != null) {
//                    for (String n : dimV.keySet()) {
//                        sb.append(",");
//                        sb.append(n).append("=").append("" + dimV.get(n));
//                    }
//                }

                sb.append("\n");
                writer.write(sb.toString());
                count++;
            }
            writer.close();
            System.out.println("Wrote " + count + " vessel names to " + cache.getAbsolutePath());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getType(String sentence) {
        String[] tk = sentence.trim().split(",");
        if (tk.length < 1)
            return "";
        return tk[0].trim();
    }
    
    /**
     * @param sentence
     * @param contactDb 
     */
    public static boolean process(String sentence) {
        String type = getType(sentence);
        if (type.isEmpty())
            return false;
        
        switch (type.toUpperCase()) {
            case "AIS":
                return parseAIS(sentence);
            case "DISTRESS_POSITION":
                return parseDistressPosition(sentence);
            case "DISTRESS_STATUS":
                return parseDistressStatus(sentence);
            case "DISTRESS_CALL":
//                return parseDistressCall(sentence);
            default:
                System.out.println("Type not known (" + type + ")");
                return false;
        }
    }

    /**
     * Example:
     * 
     * AIS,Node_Name=211212500,Node_Type=ship,Latitude=38.889712,Longitude=Â­
     * 77.008934,Depth=0,Speed=0,Heading=52.5,Course=n.a.,RateOfTurn=n.a.,Timestamp=n.a.,
     * Navigation_Status=1,Number_Contacts=1,
     * Node_Name=221212500,Node_Type=ship,
     * Latitude=38.887712,Longitude=Â­77.018934,Depth=0,Speed=0.5,Heading=62.5,
     * Course=n.a.,RateOfTurn=n.a.,Timestamp=n.a.,Navigation_Status=0\r\n
     * 
     * @param sentence
     * @param contactDb 
     */
    private static boolean parseAIS(String sentence) {
        final int AIS_ELM = 12;
        final int EXTRA_COUNTER_IDX = 13;
        final int MIN_ELMS = 14;
        String[] tk = sentence.split(",");
        if (tk.length < MIN_ELMS || !"AIS".equalsIgnoreCase(tk[0].trim()))
            return false;
        
        String[] msg = Arrays.copyOfRange(tk, 1, EXTRA_COUNTER_IDX);
        @SuppressWarnings("unused")
        boolean res = parseOneAISWorker(msg);
        int extraElements = 0;
        try {
            extraElements = Integer.parseInt(tk[EXTRA_COUNTER_IDX].split("=")[1].trim());
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        
        if (extraElements == 0)
            return true;
        if (tk.length != MIN_ELMS + AIS_ELM * extraElements)
            return false;
        
        for (int i = 0; i < extraElements; i++) {
            msg = Arrays.copyOfRange(tk, MIN_ELMS + AIS_ELM * i, MIN_ELMS + AIS_ELM * (i + 1));
            res |= parseOneAISWorker(msg);
        }
        return true;
    }

    /**
     * @param msg
     * @param contactDb
     * @return
     */
    private static boolean parseOneAISWorker(String[] msg) {
        if (msg.length < 11)
            return false;
        
        int mmsi = -1;
        String name = null;
        String type = "";
        double latDegs = 0;
        double lonDegs = 0;
        double depth = 0;
        double speedKnots = 0;
        double headingDegs = 0;
        double courseDegs = 0;
        double rateOfTurnDegsPerMin = Double.NaN;
        double timestamp = Double.NaN;
        int navStatus = -1;
        
        try {
            for (String st : msg) {
                String[] tk = st.split("=");
                String v;
                switch (tk[0].trim().toLowerCase()) {
                    case "mmsi":
                        try {
                            mmsi = Integer.parseInt(tk[1].trim());
                        }
                        catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "node_name":
                        name = tk[1].trim();
                        break;
                    case "node_type":
                        v = tk[1].trim();
                        if (v.toLowerCase().startsWith("n.a"))
                            type = "";
                        else
                            type = tk[1].trim();
                        break;
                    case "latitude":
                        latDegs = Double.parseDouble(tk[1].trim());
                        break;
                    case "longitude":
                        lonDegs = Double.parseDouble(tk[1].trim());
                        break;
                    case "depth":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            depth = Double.parseDouble(v);
                        break;
                    case "speed":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            speedKnots = Double.parseDouble(v);
                        break;
                    case "heading":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            headingDegs = AngleUtils.nomalizeAngleDegrees360(Double.parseDouble(v));
                        if (headingDegs > 360)
                            headingDegs = 0;
                        break;
                    case "course":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            courseDegs = AngleUtils.nomalizeAngleDegrees360(Double.parseDouble(v));
                        if (courseDegs > 360)
                            courseDegs = 0;
                        break;
                    case "rateofturn":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            rateOfTurnDegsPerMin = Double.parseDouble(v);
                        break;
                    case "timestamp":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            timestamp = Double.parseDouble(v);
                        break;
                    case "navigation_status":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            navStatus = Integer.parseInt(v);
                        break;
                    default:
                        System.out.println("Token not known (" + st + ")!");
                        break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        long timeMillis = System.currentTimeMillis();
        if (Double.isFinite(timestamp)) { // assuming millis since epoch
            timeMillis = Double.valueOf(timestamp).longValue();
            if (timeMillis != timestamp)
                timeMillis = Double.valueOf(timestamp * 1E3).longValue();
        }
        
        if (mmsi == -1) {
            try {
                mmsi = Integer.parseInt(name);
                String shipName = labelCache.get(mmsi);
                if (shipName != null && !shipName.isEmpty())
                    name = shipName;
            }
            catch (NumberFormatException e) {
                // e.printStackTrace();
                mmsi = name.hashCode();
            }
        }
        
        if (name == null && mmsi != -1) {
            String shipName = labelCache.get(mmsi);
            if (shipName != null && !shipName.isEmpty())
                name = shipName;
        }
        
        AISContact sys = aisDB.get(mmsi);
        if (sys == null) {
            sys = new AISContact();
            aisDB.put(mmsi, sys);
            sys.mmsid = mmsi;
        }
        
        // OK, let us fill the data
        sys.latDegs = latDegs;
        sys.lonDegs = lonDegs;
        sys.depth = depth;
        sys.timestamp = timestamp;
        sys.headingDegs = headingDegs > 360 ? courseDegs : headingDegs;
        sys.courseDegs = courseDegs;
        sys.speedKnots = speedKnots;
        sys.navStatus = navStatus;
        
        if (!type.isEmpty()) {
            sys.type = type;
        }
        
        if (Double.isFinite(rateOfTurnDegsPerMin)) {
            sys.rateOfTurnDegsPerMin = rateOfTurnDegsPerMin;;
        }
        
        return true;
    }

    /**
     * DISTRESS_POSITION,Nationality=PT,Latitude=-45.899387,Longitude=34.56787,
     *           "Depth=346,Speed=8.3,Heading=45.6\r\n
     * @param sentence
     * @return
     */
    private static boolean parseDistressPosition(String sentence) {
        final int MIN_ELMS = 7;
        final int MIN_BASE_ELMS = 6;
        String[] tk = sentence.split(",");
        if (tk.length < MIN_ELMS || !"DISTRESS_POSITION".equalsIgnoreCase(tk[0].trim()))
            return false;
        
        String[] msg = Arrays.copyOfRange(tk, 1, MIN_ELMS);
        
        int countBaseElm = 0;
        DistressPosition dp = new DistressPosition();

        try {
            for (String st : msg) {
                tk = st.split("=");
                String v;
                switch (tk[0].trim().toLowerCase()) {
                    case "nationality":
                        dp.nation = tk[1].trim();
                        countBaseElm++;
                        break;
                    case "latitude":
                        dp.latDegs = Double.parseDouble(tk[1].trim());
                        countBaseElm++;
                        break;
                    case "longitude":
                        dp.lonDegs = Double.parseDouble(tk[1].trim());
                        countBaseElm++;
                        break;
                    case "depth":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.depth = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "speed":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.speedKnots = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "heading":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.headingDegs = AngleUtils.nomalizeAngleDegrees360(Double.parseDouble(v));
                        if (dp.headingDegs > 360)
                            dp.headingDegs = 0;
                        countBaseElm++;
                        break;
                    default:
                        System.out.println("Token not known (" + st + ")!");
                        break;
                }
            }
            if (countBaseElm < MIN_BASE_ELMS)
                return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        long timeMillis = System.currentTimeMillis();
        dp.timestamp = timeMillis;
        
        distressPosition = dp;

        return true;
    }

    /**
     * "DISTRESS_STATUS,Nationality=PT,O2=17.8,CO2=5,CO=180,H2=3.5,
     *           "Pressure=42.6,Temperature=50,Survivors=43\r\n"
     * @param sentence
     * @return
     */
    private static boolean parseDistressStatus(String sentence) {
        final int MIN_ELMS = 9;
        final int MIN_BASE_ELMS = 8;
        String[] tk = sentence.split(",");
        if (tk.length < MIN_ELMS || !"DISTRESS_STATUS".equalsIgnoreCase(tk[0].trim()))
            return false;
        
        String[] msg = Arrays.copyOfRange(tk, 1, MIN_ELMS);
        
        int countBaseElm = 0;
        DistressStatus dp = new DistressStatus();

        try {
            for (String st : msg) {
                tk = st.split("=");
                String v;
                switch (tk[0].trim().toLowerCase()) {
                    case "nationality":
                        dp.nation = tk[1].trim();
                        countBaseElm++;
                        break;
                    case "o2":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.o2Percentage = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "co2":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.co2Percentage = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "co":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.coPpm = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "h2":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.h2Percentage = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "pressure":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.presureAtm = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "temperature":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.temperatureDegCentigrade = Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    case "survivors":
                        v = tk[1].trim();
                        if (!v.toLowerCase().startsWith("n.a"))
                            dp.survivors = (int) Double.parseDouble(v);
                        countBaseElm++;
                        break;
                    default:
                        System.out.println("Token not known (" + st + ")!");
                        break;
                }
            }
            if (countBaseElm < MIN_BASE_ELMS)
                return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        long timeMillis = System.currentTimeMillis();
        dp.timestamp = timeMillis;
        
        distressStatus = dp;

        return true;
    }
}
