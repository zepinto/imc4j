/*
 * Copyright (c) 2004-2017 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * Modified European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the Modified EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://github.com/LSTS/neptus/blob/develop/LICENSE.md
 * and http://ec.europa.eu/idabc/eupl.html.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: zp
 * Nov 26, 2014
 */
package pt.lsts.mvplanner;

import java.util.LinkedHashMap;

/**
 * @author zp
 *
 */
public class VehicleParams {

	public static final int SC1_ID = 0x0015;
	public static final int SC2_ID = 0x0016;
	public static final int SC3_ID = 0x0017;
	public static final int XT2_ID = 0x0018;
	public static final int ARP_ID = 0x0019;
	public static final int NP1_ID = 0x001A;
	public static final int NP2_ID = 0x001B;
	public static final int NP3_ID = 0x001C;
	public static final int XP1_ID = 0x001E;
	public static final int XP2_ID = 0x001F;

	private static LinkedHashMap<Integer, Double> batteryCapacities = new LinkedHashMap<Integer, Double>();
	static {
		batteryCapacities.put(SC1_ID, 700.0);
		batteryCapacities.put(SC2_ID, 525.0);
		batteryCapacities.put(SC3_ID, 525.0);
		batteryCapacities.put(XT2_ID, 525.0);
		batteryCapacities.put(XP1_ID, 1400.0);
		batteryCapacities.put(XP2_ID, 1400.0);
		batteryCapacities.put(NP1_ID, 700.0);
		batteryCapacities.put(NP2_ID, 700.0);
		batteryCapacities.put(NP3_ID, 700.0);
	}

	private static LinkedHashMap<Integer, String> vehicleNames = new LinkedHashMap<>();
	static {
		vehicleNames.put(SC1_ID, "lauv-seacon-1");
		vehicleNames.put(SC2_ID, "lauv-seacon-2");
		vehicleNames.put(SC3_ID, "lauv-seacon-3");
		vehicleNames.put(XT2_ID, "lauv-xtreme-2");
		vehicleNames.put(ARP_ID, "lauv-arpao");
		vehicleNames.put(NP1_ID, "lauv-noptilus-1");
		vehicleNames.put(NP2_ID, "lauv-noptilus-2");
		vehicleNames.put(NP3_ID, "lauv-noptilus-3");
		vehicleNames.put(XP1_ID, "lauv-xplore-1");
		vehicleNames.put(XP2_ID, "lauv-xplore-2");
	}

	private static LinkedHashMap<Integer, String> vehicleNicknames = new LinkedHashMap<>();
	static {
		for (int id : vehicleNames.keySet()) {
			String name = vehicleNames.get(id);
			vehicleNicknames.put(id, name.replaceFirst("lauv-", "").replaceAll("-", ""));
		}
	}

	public static int resolveNickname(String nickname) {
		for (int id : vehicleNicknames.keySet()) {
			String nick = vehicleNicknames.get(id);
			if (nick.equals(nickname))
				return id;
		}
		return 0;
	}

	private static LinkedHashMap<Integer, Double> moveConsumption = new LinkedHashMap<Integer, Double>();
	static {
		moveConsumption.put(SC1_ID, 14.0);
		moveConsumption.put(SC2_ID, 14.0);
		moveConsumption.put(SC3_ID, 14.0);
		moveConsumption.put(XT2_ID, 14.0);
		moveConsumption.put(XP1_ID, 14.0);
		moveConsumption.put(XP2_ID, 14.0);
		moveConsumption.put(NP1_ID, 14.0);
		moveConsumption.put(NP2_ID, 14.0);
		moveConsumption.put(NP3_ID, 14.0);
	}

	private static LinkedHashMap<Integer, PayloadRequirement[]> payloads = new LinkedHashMap<Integer, PayloadRequirement[]>();
	static {
		payloads.put(SC1_ID, new PayloadRequirement[] { PayloadRequirement.sidescan, PayloadRequirement.camera });
		payloads.put(SC2_ID, new PayloadRequirement[] { PayloadRequirement.ctd });
		payloads.put(SC3_ID, new PayloadRequirement[] { PayloadRequirement.ctd });
		payloads.put(XT2_ID, new PayloadRequirement[] { PayloadRequirement.sidescan });
		payloads.put(XP1_ID, new PayloadRequirement[] { PayloadRequirement.ctd });
		payloads.put(XP2_ID, new PayloadRequirement[] { PayloadRequirement.ctd, PayloadRequirement.rhodamine });
		payloads.put(NP1_ID, new PayloadRequirement[] { PayloadRequirement.sidescan, PayloadRequirement.multibeam });
		payloads.put(NP2_ID, new PayloadRequirement[] { PayloadRequirement.sidescan });
		payloads.put(NP3_ID, new PayloadRequirement[] { PayloadRequirement.multibeam, PayloadRequirement.camera });
	}

	public static PayloadRequirement[] payloadsFor(int vehicle) {
		if (payloads.containsKey(vehicle))
			return payloads.get(vehicle);
		return new PayloadRequirement[0];
	}

	public static double maxBattery(int vehicle) {
		if (batteryCapacities.containsKey(vehicle))
			return batteryCapacities.get(vehicle);
		return 500;
	}

	public static double moveConsumption(int vehicle) {
		if (moveConsumption.containsKey(vehicle))
			return moveConsumption.get(vehicle);
		return 14;
	}

	public static String vehicleName(int vehicle) {
		return vehicleNames.get(vehicle);
	}

	public static String vehicleNickname(int vehicle) {
		return vehicleNicknames.get(vehicle);
	}
	
	public static void main(String[] args) {
		for (int id : vehicleNicknames.keySet()) {
			System.out.println(id+" : "+vehicleNickname(id));
		}
	}
}
