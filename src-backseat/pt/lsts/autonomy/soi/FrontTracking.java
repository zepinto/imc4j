package pt.lsts.autonomy.soi;

import java.util.ArrayList;

import pt.lsts.imc4j.msg.ProfileSample;
import pt.lsts.imc4j.msg.VerticalProfile;
import pt.lsts.imc4j.util.WGS84Utilities;

public class FrontTracking {

	// Where to start heading
	double initialAngle = Math.toRadians(0);

	// when the fron is crossed in the topm the angle is incremented by this amount
	double angleIncrement = Math.toRadians(15);

	// Salinity threshold which means outside filament
	double minSalinity = 34.3;

	double maxSalinity = 34.5;

	// Depth (of the sample) to use for calculations
	double salinityDepth = 5;

	// Are we climbing to max salinity?
	boolean goingUp = false;

	// Maximum segment length to use
	double segmentLength = 5000;

	// A list of last crossings taken
	ArrayList<Double[]> crossings = new ArrayList<>();

	public FrontTracking(double minSalinity, double maxSalinity, double salinityDepth, double angleIncDegs,
			double initialAngleDegs, boolean goingUp) {
		this.minSalinity = minSalinity;
		this.maxSalinity = maxSalinity;
		this.salinityDepth = salinityDepth;
		this.angleIncrement = Math.toRadians(angleIncDegs);
		this.initialAngle = Math.toRadians(initialAngleDegs);
		this.goingUp = goingUp;
	}

	public double getSalinity(VerticalProfile profile) {
		double salinity = 0;
		for (ProfileSample s : profile.samples) {
			if (s.depth <= salinityDepth)
				salinity = s.avg;
			else
				break;
		}
		return salinity;
	}

	public boolean isOutside(VerticalProfile profile) {
		double salinity = getSalinity(profile);

		// didn't reach the target salinity
		if (salinity == 0)
			return false;

		if (goingUp)
			return (salinity >= maxSalinity);
		else
			return (salinity <= minSalinity);
	}

	//double[] center = new double[] { 41.183494, -8.704542 };

	public void printPos(double lat, double lon) {
		
		if (crossings.size() == 0) {
			System.out.println("Position: 0, 0");
		}
		else {
			Double[] firstCrossing = crossings.get(0);
			double[] pos = WGS84Utilities.WGS84displacement(firstCrossing[0], firstCrossing[1], 0, lat, lon, 0);
			System.out.println("Position: " + pos[0] + ", " + pos[1]);	
		}
		
	}

	// When reaches up border, the new waypoint is simular to last transet but with
	// a fixed angle increment
	public double[] newWaypointDown() {
		System.out.println("Computing waypoint down");

		double ang = Math.PI + initialAngle;

		double lastCrossingLat = crossings.get(crossings.size() - 1)[0];
		double lastCrossingLon = crossings.get(crossings.size() - 1)[1];

		System.out.println("Crossing -1:");
		printPos(lastCrossingLat, lastCrossingLon);

		if (crossings.size() >= 2) {

			double butLastCrossingLat = crossings.get(crossings.size() - 2)[0];
			double butLastCrossingLon = crossings.get(crossings.size() - 2)[1];

			System.out.println("Crossing 2:");
			printPos(butLastCrossingLat, butLastCrossingLon);

			double[] xyOffset = WGS84Utilities.WGS84displacement(lastCrossingLat, lastCrossingLon, 0, butLastCrossingLat, butLastCrossingLon, 0);
			ang = Math.atan2(xyOffset[1], xyOffset[0]);
			System.out.print("ang: " + Math.toRadians(ang) + " --> ");
			ang -= angleIncrement;
			System.out.println(Math.toRadians(ang));
		}

		System.out.println("Angle: " + Math.toDegrees(ang));
		return WGS84Utilities.WGS84displace(lastCrossingLat, lastCrossingLon, 0, Math.cos(ang) * segmentLength,
				Math.sin(ang) * segmentLength, 0);
	}

	// When reaches up border, the new waypoint is computed by calculating a normal
	// angle to the front
	public double[] newWaypointUp() {
		double lastXingLat = crossings.get(crossings.size() - 1)[0];
		double lastXingLon = crossings.get(crossings.size() - 1)[1];
		
		double ang = initialAngle;

		System.out.println("Crossing 1:");
		printPos(lastXingLat, lastXingLon);

		if (crossings.size() >= 3) {
		
			double butLastXingLat = crossings.get(crossings.size() - 1)[0];
			double butLastXingLon = crossings.get(crossings.size() - 1)[1];
			
			double butButLastXinglat = crossings.get(crossings.size() - 3)[0];
			double butButLastXingLon = crossings.get(crossings.size() - 3)[1];

			System.out.println("Crossing 2:");
			printPos(butButLastXinglat, butButLastXingLon);

			double[] xyOffset = WGS84Utilities.WGS84displacement(butButLastXinglat, butButLastXingLon, 0, lastXingLat,
					lastXingLon, 0);
			ang = (Math.atan2(xyOffset[1], xyOffset[0]) - Math.PI / 2);
	
			double[] xyOffsetPrev = WGS84Utilities.WGS84displacement(lastXingLat, lastXingLon, 0, butLastXingLat,
					butLastXingLon, 0);
	
			double angPrev = (Math.atan2(xyOffsetPrev[1], xyOffsetPrev[0]) - Math.PI / 2);
			
			if (ang < angPrev) {
				System.out.println("Since angle is lower than previous one, using previous one.");
				ang = angPrev;
			}
			
		}
		System.out.println("Angle: " + Math.toDegrees(ang));
		return WGS84Utilities.WGS84displace(lastXingLat, lastXingLon, 0, Math.cos(ang) * segmentLength,
				Math.sin(ang) * segmentLength, 0);
	}

	public void addCrossing(double lat, double lon) {
		crossings.add(new Double[] { lat, lon });
	}

	public static void main(String[] args) {

	}
}
