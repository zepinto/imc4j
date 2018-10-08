package pt.lsts.autonomy.soi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.ProfileSample;
import pt.lsts.imc4j.msg.Salinity;
import pt.lsts.imc4j.msg.VerticalProfile;
import pt.lsts.imc4j.msg.VerticalProfile.PARAMETER;
import pt.lsts.imc4j.util.WGS84Utilities;

public class VerticalProfiler<T extends Message> {

	ArrayList<ProfileSample> samples = new ArrayList<>();

	double maxDepth = 0;
	EstimatedState lastState = null;
	
	public void setSample(EstimatedState state, T value) {
		if (state == null)
			return;
		
		ProfileSample newSample = new ProfileSample();
		maxDepth = Math.max(maxDepth, state.depth);
		newSample.depth = (int) Math.round(state.depth * 10);
		newSample.avg = value.getFloat("value");
		synchronized (samples) {
			samples.add(newSample);				
		}		
		lastState = state;
	}
	
	public Map<Double, Double> getProfileMap(int numDepths) {
		synchronized (samples) {
			if (samples.isEmpty())
				return null;				
		}
		double[] sums = new double[numDepths];
		int[] counts = new int[numDepths];
		LinkedHashMap<Double, Double> res = new LinkedHashMap<>();
		
		synchronized (samples) {
			for (ProfileSample s : samples) {
				double depth = (s.depth / 10);
				int pos = (int) ((depth / maxDepth) * numDepths);
				if (pos >= numDepths)
					continue;
				counts[pos]++;
				sums[pos] += s.avg;
			}	
		}
		
		for (int pos = 0; pos < numDepths; pos++) {
			if (counts[pos] == 0)
				continue;
			res.put(((double) pos / numDepths) * maxDepth, sums[pos] / counts[pos]);
		}
		
		return res;
	}


	public VerticalProfile getProfile(VerticalProfile.PARAMETER param, int numDepths) {
		synchronized (samples) {
			if (samples.isEmpty())
				return null;				
		}
		
		double[] lld = WGS84Utilities.toLatLonDepth(lastState);
		
		VerticalProfile vp = new VerticalProfile();
		vp.parameter = param;
		double[] sums = new double[numDepths];
		int[] counts = new int[numDepths];

		synchronized (samples) {
			for (ProfileSample s : samples) {
				double depth = (s.depth / 10);
				int pos = (int) ((depth / maxDepth) * numDepths);
				if (pos >= numDepths)
					continue;
				counts[pos]++;
				sums[pos] += s.avg;
			}	
		}
		
		int depthCount = 0;

		for (int pos = 0; pos < numDepths; pos++) {
			ProfileSample sample = new ProfileSample();
			if (counts[pos] == 0)
				continue;
			sample.avg = (float) (sums[pos] / counts[pos]);
			sample.depth = (int) (10.0 * ((double) pos / numDepths) * maxDepth);
			vp.samples.add(sample);
			depthCount++;
		}

		synchronized (samples) {
			samples.clear();	
		}		
		maxDepth = 0;
		vp.numSamples = depthCount;
		vp.lat = lld[0];
		vp.lon = lld[1];
		return vp;
	}

	public static void main(String[] args) {
		VerticalProfiler<Salinity> profiler = new VerticalProfiler<>();

		for (int i = 80; i < 140; i++) {
			double depth = i / 10.0;
			double sal = Math.random() * 5 + 31;
			Salinity s = new Salinity();
			s.value = (float) sal;
			EstimatedState state = new EstimatedState();
			state.depth = (float) depth;
			profiler.setSample(state, s);
		}

		VerticalProfile vp = profiler.getProfile(PARAMETER.PROF_SALINITY, 20);

		System.out.println(vp);
	}
}
