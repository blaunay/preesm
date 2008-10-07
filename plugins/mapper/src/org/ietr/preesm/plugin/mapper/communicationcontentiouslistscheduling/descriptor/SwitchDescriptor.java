package org.ietr.preesm.plugin.mapper.communicationcontentiouslistscheduling.descriptor;

import java.util.HashMap;

public class SwitchDescriptor extends TGVertexDescriptor {

	private double averageClockCyclesPerTransfer; // unit: clock cycle

	private int portNumber;

	public SwitchDescriptor(String id, String name,
			HashMap<String, ComponentDescriptor> componentDescriptorBuffer) {
		super(id, name, componentDescriptorBuffer);
		this.type = ComponentType.Switch;
	}

	public SwitchDescriptor(String id, String name,
			HashMap<String, ComponentDescriptor> componentDescriptorBuffer,
			int clockPeriod, int dataWidth, int surface) {
		super(id, name, componentDescriptorBuffer, clockPeriod, dataWidth,
				surface);
		this.type = ComponentType.Switch;
	}

	public void setAverageClockCyclesPerTransfer(
			double averageClockCyclesPerTransfer) {
		this.averageClockCyclesPerTransfer = averageClockCyclesPerTransfer;
	}

	public double getAverageClockCyclesPerTransfer() {
		return averageClockCyclesPerTransfer;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

}
