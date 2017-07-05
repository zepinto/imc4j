package pt.lsts.mvplanner;

import pt.lsts.imc4j.util.WGS84Utilities;

public class PddlLocation {
	public String name;
	public double latDegs;
	public double lonDegs;
	
	public PddlLocation(String name, double latDegs, double lonDegs) {
		this.name = name;
		this.latDegs = latDegs;
		this.lonDegs = lonDegs;
	}
	
	public double distanceTo(PddlLocation loc) {
		return WGS84Utilities.distance(latDegs, lonDegs, loc.latDegs, loc.lonDegs);
	}
	
	@Override
	public String toString() {
		return name+"("+latDegs+", "+lonDegs+")";
	}
}
