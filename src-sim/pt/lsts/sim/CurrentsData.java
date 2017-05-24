package pt.lsts.sim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import pt.lsts.imc4j.util.WGS84Utilities;

public class CurrentsData {

	private double minDistToValue = 50;
	protected LinkedHashMap<double[], double[]> currents = new LinkedHashMap<>();
	private static final File file = new File("currents.obj");

	public void addCurrents(Map<double[], double[]> data) {
		currents.putAll(data);
		store(this);
	}

	public void addCurrent(double[] loc, double[] current) {
		currents.put(loc, current);
		store(this);
	}

	public void clearCurrents() {
		currents.clear();
		store(this);
	}

	public Map<double[], double[]> getCurrents() {
		return Collections.unmodifiableMap(currents);
	}

	public double[] getCurrent(double latDegrees, double lonDegrees) {
		if (currents.size() == 0)
			return new double[] { 0d, 0d };
		else if (currents.size() == 1)
			return currents.values().iterator().next();
		else {
			double dTotal = 0;
			double valTotal[] = new double[] { 0, 0 };

			for (Entry<double[], double[]> sounding : currents.entrySet()) {
				double dist = WGS84Utilities.distance(sounding.getKey()[0], sounding.getKey()[1], latDegrees,
						lonDegrees);

				if (dist > minDistToValue)
					continue;

				// System.out.println(sounding.getKey()[0]+",
				// "+sounding.getKey()[1]+" =
				// "+sounding.getValue()[0]+"/"+sounding.getValue()[1]);

				dist = Math.pow(dist, 3);
				if (dist > 0.0) {
					dist = 1.0 / dist;
				} else { // if d is real small set the inverse to a large number
							// to avoid INF
					dist = 1.e20;
				}
				valTotal[0] += dist * sounding.getValue()[0];
				valTotal[1] += dist * sounding.getValue()[1];
				dTotal += dist;
			}

			valTotal[0] /= dTotal;
			valTotal[1] /= dTotal;
			return valTotal;
		}
	}

	private static void store(CurrentsData obj) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(obj);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static CurrentsData load(ActionForecastModel model, int hoursInTheFuture) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd HH:00");
		String url;
		double originLat, originLon, dy, dx;

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date d = new Date(System.currentTimeMillis() + hoursInTheFuture * 3600 * 1000);
		url = model.url() + sdf.format(d).replaceAll(" ", "%20");
		URLConnection conn = new URL(url).openConnection();

		List<JsonValue> values = ((JsonArray) Json.parse(new InputStreamReader(conn.getInputStream()))).values();
		JsonObject v1 = (JsonObject) values.get(0);
		JsonObject v2 = (JsonObject) values.get(1);

		JsonObject header = (JsonObject) v1.get("header");
		JsonArray data1 = (JsonArray) v1.get("data");
		JsonArray data2 = (JsonArray) v2.get("data");
		int ny = header.getInt("ny", 0);
		// int nx = header.getInt("nx", 0);
		originLat = header.getDouble("la1", 0);
		originLon = header.getDouble("lo1", 0);
		dx = header.getDouble("dx", 0);
		dy = header.getDouble("dy", 0);

		CurrentsData sim = new CurrentsData();

		for (int i = 0; i < data1.size(); i++) {
			int x = i / ny;
			int y = i % ny;

			JsonValue val1 = data1.get(i);
			JsonValue val2 = data2.get(i);
			if (!val1.isNull()) {

				double[] coords = new double[] { originLat - y * dy, originLon + x * dx };
				double[] current = new double[] { val1.asDouble(), val2.asDouble() };
				sim.currents.put(coords, current);
			}
		}

		return sim;
	}

	public static CurrentsData load() {
		if (!file.canRead())
			return new CurrentsData();
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			CurrentsData obj = (CurrentsData) ois.readObject();
			ois.close();
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			return new CurrentsData();
		}
	}

	enum ActionForecastModel {
		Douro("http://api.actionmodulers.com/api/geometry/GetVelocityVectorByModelDomainAndTime/?&modelDomainName=Douro%20Level%201&instant="), Tagus(
				"http://api.actionmodulers.com/api/geometry/GetVelocityVectorByModelDomainAndTime/?&modelDomainName=Douro%20Level%201&instant="), Portugal(
						"http://api.actionmodulers.com/api/geometry/GetVelocityVectorByModelDomainAndTime/?&modelDomainName=Douro%20Level%201&instant=");

		private String url;

		private ActionForecastModel(String url) {
			this.url = url;
		}

		public String url() {
			return url;
		}
	}

	public static void main(String[] args) throws Exception {
		CurrentsData simulator = CurrentsData.load(ActionForecastModel.Douro, 0);

		double[] coords1 = new double[] { 41.1761, -8.7072 };
		double[] coords2 = new double[] { 41.1771, -8.7137 };
		double[] coords3 = new double[] { 41.1771, -8.7032 };
		double[] coords4 = new double[] { 41.1, -8.68 };

		System.out.println(simulator.getCurrent(coords1[0], coords1[1])[0] + ", "
				+ simulator.getCurrent(coords1[0], coords1[1])[1]);
		System.out.println(simulator.getCurrent(coords2[0], coords2[1])[0] + ", "
				+ simulator.getCurrent(coords2[0], coords2[1])[1]);
		System.out.println(simulator.getCurrent(coords3[0], coords3[1])[0] + ", "
				+ simulator.getCurrent(coords3[0], coords3[1])[1]);
		System.out.println(simulator.getCurrent(coords4[0], coords4[1])[0] + ", "
				+ simulator.getCurrent(coords4[0], coords4[1])[1]);
	}
}
