package pt.lsts.autonomy.soi;

import java.util.Random;

import pt.lsts.imc4j.msg.ProfileSample;
import pt.lsts.imc4j.msg.VerticalProfile;

public class FrontTrackingTest {

	public static void testOutside() {
		FrontTracking tracking = new FrontTracking(31.3, 31.5, 5, 15, 0, true);
		Random r = new Random(System.currentTimeMillis());
		
		VerticalProfile vp = new VerticalProfile();
		double depthInc = 4.7;
		
		for (int i = 0; i < 20; i++) {
			ProfileSample sample = new ProfileSample();
			sample.depth = (int) (i * depthInc);
			sample.avg = 34 + r.nextFloat() / 2;
			System.out.println(sample.depth+" - "+sample.avg);
			vp.samples.add(sample);
		}
		
		System.out.println("Is outside? "+tracking.isOutside(vp));
	}
	
	public static void testWaypoints() {
		System.out.println("Test Waypoints");
		double[] wpt1 = new double[] {31.069656, -131.99525};
		FrontTracking tracking = new FrontTracking(31.3, 31.5, 5, 15, 0, true);
		tracking.addCrossing(wpt1[0], wpt1[1]);
		
		System.out.println(tracking.newWaypointDown()[0]+", "+tracking.newWaypointDown()[1]);
		tracking.addCrossing(tracking.newWaypointDown()[0], tracking.newWaypointDown()[1]);
		
		System.out.println(tracking.newWaypointUp()[0]+", "+tracking.newWaypointUp()[1]);
		tracking.addCrossing(tracking.newWaypointUp()[0], tracking.newWaypointUp()[1]);
	}
	
	public static void testWaypointUp() {
		double[] wpt1 = new double[] {31.069656, -131.99525};
		double[] wpt2 = new double[] {31.126836, -131.995336};
		double[] wpt3 = new double[] {31.065356, -131.976325};
		double[] wpt4 = new double[] {31.120442, -131.961692};

		FrontTracking tracking = new FrontTracking(34.3, 34.5, 5, 15, 0, true);
		
		tracking.addCrossing(wpt1[0], wpt1[1]);
		tracking.addCrossing(wpt2[0], wpt2[1]);
		System.out.println(tracking.newWaypointDown()[0]+", "+tracking.newWaypointDown()[1]);
		
		tracking.addCrossing(wpt3[0], wpt3[1]);
		System.out.println(tracking.newWaypointUp()[0]+", "+tracking.newWaypointUp()[1]);
	}
	
	
	public static void main(String[] args) {
		testOutside();
		testWaypointUp();
		testWaypoints();
	}
	
}
