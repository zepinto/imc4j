package pt.lsts.imc4j.backseat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import pt.lsts.autonomy.soi.VerticalProfiler;
import pt.lsts.imc4j.msg.EstimatedState;
import pt.lsts.imc4j.msg.ProfileSample;
import pt.lsts.imc4j.msg.Temperature;
import pt.lsts.imc4j.msg.VerticalProfile;
import pt.lsts.imc4j.msg.VerticalProfile.PARAMETER;
import pt.lsts.imc4j.util.FormatConversion;

public class VerticalProfileTest {

	static Random r = new Random(System.currentTimeMillis());

	static float getTemp(double depth) {
		return (float) (13 + depth / 10 + r.nextGaussian());
	}

	public static void main(String[] args) throws ParseException {
		VerticalProfiler<Temperature> profiler = new VerticalProfiler<>();

		for (float i = 0; i < 100; i += 0.3) {
			EstimatedState state = new EstimatedState();
			state.depth = i;
			Temperature t = new Temperature();
			t.value = getTemp(i);
			profiler.setSample(state, t);
		}

		for (float i = 100; i > 0; i -= 0.3) {
			EstimatedState state = new EstimatedState();
			state.depth = i;
			Temperature t = new Temperature();
			t.value = getTemp(i);
			profiler.setSample(state, t);
		}

		VerticalProfile vp = profiler.getProfile(PARAMETER.PROF_TEMPERATURE, 25);
		System.out.println(vp);

		// for (ProfileSample s : vp.samples) {
		// System.out.println(s.depth/10.0 +" -> "+s.avg);
		// }

		ArrayList<Double> xData = new ArrayList<>();
		ArrayList<Double> yData = new ArrayList<>();

		String[] profiles = new String[] {
				vp.toString(),
				"{\"abbrev\":\"VerticalProfile\",\"timestamp\":1.523352589833E9,\"src\":65535,\"src_ent\":255,\"dst\":65535,\"dst_ent\":255,\"parameter\":\"PROF_TEMPERATURE\",\"numSamples\":20,\"samples\":[{\"abbrev\":\"ProfileSample\",\"depth\":0,\"avg\":13.8295555},{\"abbrev\":\"ProfileSample\",\"depth\":39,\"avg\":13.783368},{\"abbrev\":\"ProfileSample\",\"depth\":79,\"avg\":13.788055},{\"abbrev\":\"ProfileSample\",\"depth\":119,\"avg\":13.78335},{\"abbrev\":\"ProfileSample\",\"depth\":159,\"avg\":13.765636},{\"abbrev\":\"ProfileSample\",\"depth\":199,\"avg\":13.696667},{\"abbrev\":\"ProfileSample\",\"depth\":239,\"avg\":13.667682},{\"abbrev\":\"ProfileSample\",\"depth\":279,\"avg\":13.641909},{\"abbrev\":\"ProfileSample\",\"depth\":319,\"avg\":13.615956},{\"abbrev\":\"ProfileSample\",\"depth\":359,\"avg\":13.589783},{\"abbrev\":\"ProfileSample\",\"depth\":398,\"avg\":13.5459585},{\"abbrev\":\"ProfileSample\",\"depth\":438,\"avg\":13.523217},{\"abbrev\":\"ProfileSample\",\"depth\":478,\"avg\":13.4978},{\"abbrev\":\"ProfileSample\",\"depth\":518,\"avg\":13.470291},{\"abbrev\":\"ProfileSample\",\"depth\":558,\"avg\":13.43908},{\"abbrev\":\"ProfileSample\",\"depth\":598,\"avg\":13.41792},{\"abbrev\":\"ProfileSample\",\"depth\":638,\"avg\":13.405541},{\"abbrev\":\"ProfileSample\",\"depth\":678,\"avg\":13.39175},{\"abbrev\":\"ProfileSample\",\"depth\":718,\"avg\":13.360042},{\"abbrev\":\"ProfileSample\",\"depth\":757,\"avg\":13.33964}],\"lat\":38.408990905144364,\"lon\":-9.110152688849867}",
				"{\"abbrev\":\"VerticalProfile\",\"timestamp\":1.523351922835E9,\"src\":65535,\"src_ent\":255,\"dst\":65535,\"dst_ent\":255,\"parameter\":\"PROF_TEMPERATURE\",\"numSamples\":20,\"samples\":[{\"abbrev\":\"ProfileSample\",\"depth\":0,\"avg\":13.80075},{\"abbrev\":\"ProfileSample\",\"depth\":20,\"avg\":13.801084},{\"abbrev\":\"ProfileSample\",\"depth\":41,\"avg\":13.8046665},{\"abbrev\":\"ProfileSample\",\"depth\":61,\"avg\":13.80225},{\"abbrev\":\"ProfileSample\",\"depth\":82,\"avg\":13.806923},{\"abbrev\":\"ProfileSample\",\"depth\":102,\"avg\":13.800167},{\"abbrev\":\"ProfileSample\",\"depth\":123,\"avg\":13.770166},{\"abbrev\":\"ProfileSample\",\"depth\":143,\"avg\":13.754308},{\"abbrev\":\"ProfileSample\",\"depth\":164,\"avg\":13.74875},{\"abbrev\":\"ProfileSample\",\"depth\":184,\"avg\":13.705},{\"abbrev\":\"ProfileSample\",\"depth\":205,\"avg\":13.675693},{\"abbrev\":\"ProfileSample\",\"depth\":225,\"avg\":13.664909},{\"abbrev\":\"ProfileSample\",\"depth\":246,\"avg\":13.658462},{\"abbrev\":\"ProfileSample\",\"depth\":267,\"avg\":13.649417},{\"abbrev\":\"ProfileSample\",\"depth\":287,\"avg\":13.6385},{\"abbrev\":\"ProfileSample\",\"depth\":308,\"avg\":13.627666},{\"abbrev\":\"ProfileSample\",\"depth\":328,\"avg\":13.623154},{\"abbrev\":\"ProfileSample\",\"depth\":349,\"avg\":13.619083},{\"abbrev\":\"ProfileSample\",\"depth\":369,\"avg\":13.604055},{\"abbrev\":\"ProfileSample\",\"depth\":390,\"avg\":13.586445}],\"lat\":38.408971965024634,\"lon\":-9.118358634501204}",
				"{\"abbrev\":\"VerticalProfile\",\"timestamp\":1.523351664836E9,\"src\":65535,\"src_ent\":255,\"dst\":65535,\"dst_ent\":255,\"parameter\":\"PROF_TEMPERATURE\",\"numSamples\":20,\"samples\":[{\"abbrev\":\"ProfileSample\",\"depth\":0,\"avg\":13.152369},{\"abbrev\":\"ProfileSample\",\"depth\":39,\"avg\":13.701103},{\"abbrev\":\"ProfileSample\",\"depth\":79,\"avg\":13.704658},{\"abbrev\":\"ProfileSample\",\"depth\":119,\"avg\":13.707803},{\"abbrev\":\"ProfileSample\",\"depth\":159,\"avg\":13.706025},{\"abbrev\":\"ProfileSample\",\"depth\":199,\"avg\":13.70598},{\"abbrev\":\"ProfileSample\",\"depth\":238,\"avg\":13.688604},{\"abbrev\":\"ProfileSample\",\"depth\":278,\"avg\":13.674326},{\"abbrev\":\"ProfileSample\",\"depth\":318,\"avg\":13.655957},{\"abbrev\":\"ProfileSample\",\"depth\":358,\"avg\":13.627461},{\"abbrev\":\"ProfileSample\",\"depth\":398,\"avg\":13.578643},{\"abbrev\":\"ProfileSample\",\"depth\":437,\"avg\":13.534138},{\"abbrev\":\"ProfileSample\",\"depth\":477,\"avg\":13.496793},{\"abbrev\":\"ProfileSample\",\"depth\":517,\"avg\":13.455259},{\"abbrev\":\"ProfileSample\",\"depth\":557,\"avg\":13.434587},{\"abbrev\":\"ProfileSample\",\"depth\":597,\"avg\":13.40975},{\"abbrev\":\"ProfileSample\",\"depth\":636,\"avg\":13.39825},{\"abbrev\":\"ProfileSample\",\"depth\":676,\"avg\":13.395036},{\"abbrev\":\"ProfileSample\",\"depth\":716,\"avg\":13.390965},{\"abbrev\":\"ProfileSample\",\"depth\":756,\"avg\":13.372334}],\"lat\":38.410506776021016,\"lon\":-9.115328075479502}",
				"{\"abbrev\":\"VerticalProfile\",\"timestamp\":1.523354060833E9,\"src\":65535,\"src_ent\":255,\"dst\":65535,\"dst_ent\":255,\"parameter\":\"PROF_TEMPERATURE\",\"numSamples\":20,\"samples\":[{\"abbrev\":\"ProfileSample\",\"depth\":0,\"avg\":13.741673},{\"abbrev\":\"ProfileSample\",\"depth\":49,\"avg\":13.757488},{\"abbrev\":\"ProfileSample\",\"depth\":99,\"avg\":13.736766},{\"abbrev\":\"ProfileSample\",\"depth\":149,\"avg\":13.717609},{\"abbrev\":\"ProfileSample\",\"depth\":199,\"avg\":13.680715},{\"abbrev\":\"ProfileSample\",\"depth\":248,\"avg\":13.66153},{\"abbrev\":\"ProfileSample\",\"depth\":298,\"avg\":13.597417},{\"abbrev\":\"ProfileSample\",\"depth\":348,\"avg\":13.570056},{\"abbrev\":\"ProfileSample\",\"depth\":398,\"avg\":13.537628},{\"abbrev\":\"ProfileSample\",\"depth\":447,\"avg\":13.499306},{\"abbrev\":\"ProfileSample\",\"depth\":497,\"avg\":13.446829},{\"abbrev\":\"ProfileSample\",\"depth\":547,\"avg\":13.424028},{\"abbrev\":\"ProfileSample\",\"depth\":597,\"avg\":13.397114},{\"abbrev\":\"ProfileSample\",\"depth\":647,\"avg\":13.35825},{\"abbrev\":\"ProfileSample\",\"depth\":696,\"avg\":13.329943},{\"abbrev\":\"ProfileSample\",\"depth\":746,\"avg\":13.303361},{\"abbrev\":\"ProfileSample\",\"depth\":796,\"avg\":13.297485},{\"abbrev\":\"ProfileSample\",\"depth\":846,\"avg\":13.268833},{\"abbrev\":\"ProfileSample\",\"depth\":895,\"avg\":13.256941},{\"abbrev\":\"ProfileSample\",\"depth\":945,\"avg\":13.251941}],\"lat\":38.40854085040449,\"lon\":-9.11677711179837}",
				"{\"abbrev\":\"VerticalProfile\",\"timestamp\":1.523354913835E9,\"src\":65535,\"src_ent\":255,\"dst\":65535,\"dst_ent\":255,\"parameter\":\"PROF_TEMPERATURE\",\"numSamples\":20,\"samples\":[{\"abbrev\":\"ProfileSample\",\"depth\":0,\"avg\":13.799942},{\"abbrev\":\"ProfileSample\",\"depth\":49,\"avg\":13.778592},{\"abbrev\":\"ProfileSample\",\"depth\":99,\"avg\":13.7495},{\"abbrev\":\"ProfileSample\",\"depth\":149,\"avg\":13.706405},{\"abbrev\":\"ProfileSample\",\"depth\":199,\"avg\":13.6671295},{\"abbrev\":\"ProfileSample\",\"depth\":248,\"avg\":13.630567},{\"abbrev\":\"ProfileSample\",\"depth\":298,\"avg\":13.586968},{\"abbrev\":\"ProfileSample\",\"depth\":348,\"avg\":13.537},{\"abbrev\":\"ProfileSample\",\"depth\":398,\"avg\":13.503285},{\"abbrev\":\"ProfileSample\",\"depth\":448,\"avg\":13.477823},{\"abbrev\":\"ProfileSample\",\"depth\":497,\"avg\":13.448941},{\"abbrev\":\"ProfileSample\",\"depth\":547,\"avg\":13.421543},{\"abbrev\":\"ProfileSample\",\"depth\":597,\"avg\":13.377353},{\"abbrev\":\"ProfileSample\",\"depth\":647,\"avg\":13.3611145},{\"abbrev\":\"ProfileSample\",\"depth\":696,\"avg\":13.323206},{\"abbrev\":\"ProfileSample\",\"depth\":746,\"avg\":13.307618},{\"abbrev\":\"ProfileSample\",\"depth\":796,\"avg\":13.294628},{\"abbrev\":\"ProfileSample\",\"depth\":846,\"avg\":13.268343},{\"abbrev\":\"ProfileSample\",\"depth\":896,\"avg\":13.255657},{\"abbrev\":\"ProfileSample\",\"depth\":945,\"avg\":13.251819}],\"lat\":38.40891729673025,\"lon\":-9.108754593891412}",
				"{\"abbrev\":\"VerticalProfile\",\"timestamp\":1.523357964833E9,\"src\":65535,\"src_ent\":255,\"dst\":65535,\"dst_ent\":255,\"parameter\":\"PROF_TEMPERATURE\",\"numSamples\":20,\"samples\":[{\"abbrev\":\"ProfileSample\",\"depth\":0,\"avg\":13.763629},{\"abbrev\":\"ProfileSample\",\"depth\":49,\"avg\":13.754423},{\"abbrev\":\"ProfileSample\",\"depth\":99,\"avg\":13.747741},{\"abbrev\":\"ProfileSample\",\"depth\":149,\"avg\":13.720281},{\"abbrev\":\"ProfileSample\",\"depth\":199,\"avg\":13.622966},{\"abbrev\":\"ProfileSample\",\"depth\":249,\"avg\":13.576445},{\"abbrev\":\"ProfileSample\",\"depth\":298,\"avg\":13.542758},{\"abbrev\":\"ProfileSample\",\"depth\":348,\"avg\":13.516118},{\"abbrev\":\"ProfileSample\",\"depth\":398,\"avg\":13.490171},{\"abbrev\":\"ProfileSample\",\"depth\":448,\"avg\":13.458889},{\"abbrev\":\"ProfileSample\",\"depth\":498,\"avg\":13.419111},{\"abbrev\":\"ProfileSample\",\"depth\":547,\"avg\":13.35625},{\"abbrev\":\"ProfileSample\",\"depth\":597,\"avg\":13.301444},{\"abbrev\":\"ProfileSample\",\"depth\":647,\"avg\":13.3156},{\"abbrev\":\"ProfileSample\",\"depth\":697,\"avg\":13.290648},{\"abbrev\":\"ProfileSample\",\"depth\":747,\"avg\":13.276834},{\"abbrev\":\"ProfileSample\",\"depth\":797,\"avg\":13.268723},{\"abbrev\":\"ProfileSample\",\"depth\":846,\"avg\":13.263257},{\"abbrev\":\"ProfileSample\",\"depth\":896,\"avg\":13.255772},{\"abbrev\":\"ProfileSample\",\"depth\":946,\"avg\":13.2516}],\"lat\":38.40892123061591,\"lon\":-9.10870250637915}",
				"{\"abbrev\":\"VerticalProfile\",\"timestamp\":1.523357042833E9,\"src\":65535,\"src_ent\":255,\"dst\":65535,\"dst_ent\":255,\"parameter\":\"PROF_TEMPERATURE\",\"numSamples\":20,\"samples\":[{\"abbrev\":\"ProfileSample\",\"depth\":0,\"avg\":13.730928},{\"abbrev\":\"ProfileSample\",\"depth\":49,\"avg\":13.74029},{\"abbrev\":\"ProfileSample\",\"depth\":99,\"avg\":13.7357},{\"abbrev\":\"ProfileSample\",\"depth\":149,\"avg\":13.722813},{\"abbrev\":\"ProfileSample\",\"depth\":199,\"avg\":13.652794},{\"abbrev\":\"ProfileSample\",\"depth\":249,\"avg\":13.5875},{\"abbrev\":\"ProfileSample\",\"depth\":299,\"avg\":13.562361},{\"abbrev\":\"ProfileSample\",\"depth\":348,\"avg\":13.5158615},{\"abbrev\":\"ProfileSample\",\"depth\":398,\"avg\":13.4864855},{\"abbrev\":\"ProfileSample\",\"depth\":448,\"avg\":13.462194},{\"abbrev\":\"ProfileSample\",\"depth\":498,\"avg\":13.429919},{\"abbrev\":\"ProfileSample\",\"depth\":548,\"avg\":13.389529},{\"abbrev\":\"ProfileSample\",\"depth\":598,\"avg\":13.334222},{\"abbrev\":\"ProfileSample\",\"depth\":648,\"avg\":13.290703},{\"abbrev\":\"ProfileSample\",\"depth\":697,\"avg\":13.28975},{\"abbrev\":\"ProfileSample\",\"depth\":747,\"avg\":13.280084},{\"abbrev\":\"ProfileSample\",\"depth\":797,\"avg\":13.2716},{\"abbrev\":\"ProfileSample\",\"depth\":847,\"avg\":13.265314},{\"abbrev\":\"ProfileSample\",\"depth\":897,\"avg\":13.262},{\"abbrev\":\"ProfileSample\",\"depth\":947,\"avg\":13.253028}],\"lat\":38.40877197536548,\"lon\":-9.116336465645224}",
		};

		for (String p : profiles) {
			xData.clear();
			yData.clear();
			vp = (VerticalProfile) FormatConversion.fromJson(p);
			for (ProfileSample s : vp.samples) {
				xData.add((double) s.avg);
				yData.add((double) -s.depth / 10.0);
			}

			XYChart chart = QuickChart.getChart(new Date((long)(1000 *vp.timestamp)).toString(), "Temperature", "Depth", "Temperature", xData, yData);
			new SwingWrapper<>(chart).displayChart();
		}

	}

}
