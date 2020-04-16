package pt.lsts.simulation;

import java.util.concurrent.ConcurrentLinkedDeque;

import pt.lsts.backseat.BackSeatDriver;
import pt.lsts.imc4j.annotations.Consume;
import pt.lsts.imc4j.annotations.Periodic;
import pt.lsts.imc4j.msg.IridiumMsgTx;
import pt.lsts.imc4j.msg.VehicleMedium;
import pt.lsts.imc4j.msg.VehicleMedium.MEDIUM;

public class IridiumSimulator {

	private final BackSeatDriver connection;
	private final ConcurrentLinkedDeque<IridiumMsgTx> requests = new ConcurrentLinkedDeque<>();
	
	
	public IridiumSimulator(final BackSeatDriver connection) {
		this.connection = connection;
	}
	
	@Periodic(2000)
	public void update() {
		VehicleMedium medium = connection.get(VehicleMedium.class);
		if (medium == null) {
			connection.print("Could not obtain VehicleMedium.");
			return;
		}
		
		if (medium.medium == MEDIUM.VM_UNDERWATER)
			return;
		
		
		
	}
	
	
	@Consume
	public void on(IridiumMsgTx request) {
		
	}
	
	
	
}
