package pt.lsts.endurance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class EnduranceWebApi {

	private static final String SOI_URL = "http://ripples.lsts.pt/soi";

	public static Future<List<Asset>> getSoiState() {
		return execute(new Callable<List<Asset>>() {
			@Override
			public List<Asset> call() throws Exception {
				URL url = new URL(SOI_URL);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				conn.disconnect();
				System.out.println(content);
				return AssetState.parseStates(content.toString());
			}
		});
	}

	private static <T> Future<T> execute(Callable<T> call) {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		Future<T> ret = exec.submit(call);
		exec.shutdown();
		return ret;
	}

	public static Future<Void> setAssetState(String assetName, AssetState state) {
		JsonObject json = new JsonObject();
		json.add("name", assetName);
		json.add("received", Json.parse(state.toString()));
		return postJson(SOI_URL, json.toString());
	}	
	
	public static Future<Void> setAssetPlan(String assetName, Plan plan) {
		JsonObject json = new JsonObject();
		json.add("name", assetName);
		json.add("plan", Json.parse(plan.toString()));
		return postJson(SOI_URL, json.toString());
	}

	private static Future<Void> postJson(String url, String json) {
		return execute(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				URL url_ = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) url_.openConnection();
				byte[] data = json.getBytes(StandardCharsets.UTF_8);

				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
				conn.setFixedLengthStreamingMode(data.length);
				conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

				OutputStream os = conn.getOutputStream();
				os.write(data);
				os.close();

				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				conn.disconnect();
				System.out.println(conn.getResponseCode());
				return null;
			}
		});
	}

	public static Future<Void> setAsset(Asset asset) {
		return postJson(SOI_URL, asset.toString());
	}

	public static void main(String[] args) throws Exception {
		Asset asset = new Asset("lauv-xplore-1");
		asset.setState(AssetState.builder()
				.withLatitude(41)
				.withLongitude(-8)
				.withTimestamp(new Date())
				.build());
		
		EnduranceWebApi.setAsset(asset).get(1000, TimeUnit.MILLISECONDS);
		System.out.println(EnduranceWebApi.getSoiState().get(1000, TimeUnit.MILLISECONDS));
	}
}
