/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 *********************************************************/
 
package org.ietr.preesm.plugin.mapper.commcontenlistsched;

import java.util.HashMap;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.ArchitectureComponentType;
import org.ietr.preesm.core.architecture.Interconnection;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ArchitectureDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.BusDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ComponentDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.ProcessorDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.SwitchDescriptor;
import org.ietr.preesm.plugin.mapper.commcontenlistsched.descriptor.TGVertexDescriptor;

/**
 * @author pmu
 * 
 *         The ArchitectureTransformer converts different architectures between
 *         MultiCoreArchitecture and AlgorithmDescriptor
 */
public class ArchitectureTransformer {

	private HashMap<String, ComponentDescriptor> ComponentDescriptorBuffer;

	public ArchitectureTransformer() {
	}

	public ArchitectureDescriptor architecture2Descriptor(
			MultiCoreArchitecture architecture) {
		MultiCoreArchitecture archi = (MultiCoreArchitecture) architecture;
		ArchitectureDescriptor archiDescriptor = new ArchitectureDescriptor();
		this.ComponentDescriptorBuffer = archiDescriptor.getComponents();
		for (ArchitectureComponent indexOperator : archi
				.getComponents(ArchitectureComponentType.operator)) {
			new ProcessorDescriptor(indexOperator.getName(), indexOperator
					.getDefinition().getId(), ComponentDescriptorBuffer);
		}
		for (ArchitectureComponent indexMedium : archi
				.getComponents(ArchitectureComponentType.medium)) {
			new BusDescriptor(indexMedium.getName(), indexMedium
					.getDefinition().getId(), ComponentDescriptorBuffer);
		}
		for (ArchitectureComponent indexSwitch : archi
				.getComponents(ArchitectureComponentType.communicationNode)) {
			new SwitchDescriptor(indexSwitch.getName(), indexSwitch
					.getDefinition().getId(), ComponentDescriptorBuffer);
		}
		for (Interconnection indexInterconnection : archi.getInterconnections()) {
			if (indexInterconnection
					.getInterface(ArchitectureComponentType.operator) != null) {
				((TGVertexDescriptor) ComponentDescriptorBuffer
						.get(indexInterconnection.getInterface(
								ArchitectureComponentType.operator).getOwner()
								.getName()))
						.addInputLink((BusDescriptor) ComponentDescriptorBuffer
								.get(indexInterconnection.getInterface(
										ArchitectureComponentType.medium)
										.getOwner().getName()));
				((TGVertexDescriptor) ComponentDescriptorBuffer
						.get(indexInterconnection.getInterface(
								ArchitectureComponentType.operator).getOwner()
								.getName()))
						.addOutputLink((BusDescriptor) ComponentDescriptorBuffer
								.get(indexInterconnection.getInterface(
										ArchitectureComponentType.medium)
										.getOwner().getName()));
				((BusDescriptor) ComponentDescriptorBuffer
						.get(indexInterconnection.getInterface(
								ArchitectureComponentType.medium).getOwner()
								.getName()))
						.addTGVertex((TGVertexDescriptor) ComponentDescriptorBuffer
								.get(indexInterconnection.getInterface(
										ArchitectureComponentType.operator)
										.getOwner().getName()));
			}
		}
		return archiDescriptor;
	}

	public MultiCoreArchitecture descriptor2Architecture(
			ArchitectureDescriptor archiDescriptor) {
		MultiCoreArchitecture architecture = new MultiCoreArchitecture(
				archiDescriptor.getName());
		return architecture;
	}
}
