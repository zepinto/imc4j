package pt.lsts.autonomy.soi;

import java.util.ArrayList;

import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.Message;
import pt.lsts.imc4j.msg.ProfileSample;
import pt.lsts.imc4j.msg.Salinity;
import pt.lsts.imc4j.msg.VerticalProfile;
import pt.lsts.imc4j.msg.VerticalProfile.PARAMETER;

public class VerticalProfiler<T extends Message> {

	ArrayList<ProfileSample> samples = new ArrayList<>();

	double maxDepth = 0;

	public void setSample(EstimatedState state, T value) {
		ProfileSample newSample = new ProfileSample();
		maxDepth = Math.max(maxDepth, state.depth);
		newSample.depth = (int) Math.round(state.depth * 10);
		newSample.avg = value.getFloat("value");
		samples.add(newSample);
	}

	public VerticalProfile getProfile(VerticalProfile.PARAMETER param, int numDepths) {
		VerticalProfile vp = new VerticalProfile();
		vp.parameter = param;
		double[] sums = new double[numDepths];
		int[] counts = new int[numDepths];

		for (ProfileSample s : samples) {
			double depth = (s.depth / 10);
			int pos = (int) ((depth / maxDepth) * numDepths);
			counts[pos]++;
			sums[pos] += s.avg;
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

		samples.clear();
		maxDepth = 0;
		vp.numSamples = depthCount;
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
