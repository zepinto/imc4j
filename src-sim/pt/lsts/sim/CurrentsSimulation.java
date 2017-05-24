package pt.lsts.sim;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.squareup.otto.Subscribe;

import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.annotations.Publish;
import pt.lsts.imc4j.def.SystemType;
import pt.lsts.imc4j.msg.Announce;
import pt.lsts.imc4j.msg.EntityParameter;
import pt.lsts.imc4j.msg.SetEntityParameters;
import pt.lsts.imc4j.runtime.actors.AbstractActor;
import pt.lsts.imc4j.runtime.actors.ActorContext;
import pt.lsts.sim.CurrentsData.ActionForecastModel;

public class CurrentsSimulation extends AbstractActor {

	private LinkedHashMap<String, Announce> announces = new LinkedHashMap<>();
	private CurrentsData currents = null;

	public CurrentsSimulation(ActorContext context) {
		super(context);
	}

	@Override
	public void init() {
		super.init();
		updateCurrents();
	}
	
	@Subscribe
	public void on(Announce announce) {
		if (announce.sys_type == SystemType.USV || announce.sys_type == SystemType.UUV) {
			synchronized (announces) {
				announces.put(announce.sys_name, announce);
			}				
		}
	}

	@Periodic(1800 * 1000)
	public void updateCurrents() {
		try {
			currents = CurrentsData.load(ActionForecastModel.Douro, 0);
			System.out.println("Currents have been successfully updated.");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Periodic(60000)
	@Publish(SetEntityParameters.class)
	public void sendCurrents() {
		ArrayList<Announce> announcesReceived = new ArrayList<>();
		synchronized (announces) {
			announcesReceived.addAll(announces.values());
			announces.clear();
		}

		for (Announce announce : announcesReceived) {
			double[] current = currents.getCurrent(Math.toDegrees(announce.lat), Math.toDegrees(announce.lon));
			if (Double.isNaN(current[0])) {
				return;
			}
			else {
				SetEntityParameters params = new SetEntityParameters();
				params.name = "Simulation Engine";
				EntityParameter p1 = new EntityParameter();
				p1.name = "Stream Speed North";
				p1.value = ""+current[0];
				params.params.add(p1);
				
				EntityParameter p2 = new EntityParameter();
				p2.name = "Stream Speed East";
				p2.value = ""+current[1];
				params.params.add(p2);
				try {
					send(announce.sys_name, params);
					System.out.println("Current for "+announce.sys_name+" is "+current[0]+", "+current[1]);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		AbstractActor.exec(CurrentsSimulation.class);
	}

}
