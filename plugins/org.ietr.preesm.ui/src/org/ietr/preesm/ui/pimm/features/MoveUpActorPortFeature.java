/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot
 * 
 * [mpelcat,jnezan,kdesnos,jheulot]@insa-rennes.fr
 * 
 * This software is a computer program whose purpose is to prototype
 * parallel applications.
 * 
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 ******************************************************************************/
package org.ietr.preesm.ui.pimm.features;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.ietr.preesm.experiment.model.pimm.ConfigInputPort;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputPort;
import org.ietr.preesm.experiment.model.pimm.DataInputPort;
import org.ietr.preesm.experiment.model.pimm.DataOutputPort;
import org.ietr.preesm.experiment.model.pimm.Dependency;
import org.ietr.preesm.experiment.model.pimm.ExecutableActor;
import org.ietr.preesm.experiment.model.pimm.Port;
import org.ietr.preesm.experiment.model.pimm.util.PiIdentifiers;

/**
 * Custom feature to move up a port.
 * 
 * @author jheulot
 * 
 */
public class MoveUpActorPortFeature extends AbstractCustomFeature {

	protected boolean hasDoneChanges = false;

	/**
	 * Default Constructor
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public MoveUpActorPortFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Move up Port";
	}

	@Override
	public String getDescription() {
		return "Move up the Port";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		// allow move up if exactly one pictogram element
		// representing a Port is selected
		// and it is not the first port
		boolean ret = false;
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof Port) {
				Port port = (Port) bo;
				if (port.eContainer() instanceof ExecutableActor) {
					ExecutableActor actor = (ExecutableActor) (port
							.eContainer());
					switch (port.getKind()) {
					case PiIdentifiers.DATA_INPUT_PORT:
						ret = actor.getDataInputPorts().size() > 1;
						ret = ret
								&& actor.getDataInputPorts().indexOf(port) > 0;
						break;
					case PiIdentifiers.DATA_OUTPUT_PORT:
						ret = actor.getDataOutputPorts().size() > 1;
						ret = ret
								&& actor.getDataOutputPorts().indexOf(port) > 0;
						break;
					case PiIdentifiers.CONFIGURATION_INPUT_PORT:
						ret = actor.getConfigInputPorts().size() > 1;
						ret = ret
								&& actor.getConfigInputPorts().indexOf(port) > 0;
						break;
					case PiIdentifiers.CONFIGURATION_OUPUT_PORT:
						ret = actor.getConfigOutputPorts().size() > 1;
						ret = ret
								&& actor.getConfigOutputPorts().indexOf(port) > 0;
						break;
					}
				}
			}
		}
		return ret;
	}

	protected boolean avoidNegativeCoordinates() {
		return false;
	}

	@Override
	public void execute(ICustomContext context) {
		// Re-check if only one element is selected
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof Port) {
				Port portToMoveUp = null, portToMoveDown = null;
				int portToMoveUpIndex = -1, portToMoveDownIndex = -1;
				ExecutableActor actor;

				portToMoveUp = (Port) bo;
				actor = (ExecutableActor) (portToMoveUp.eContainer());
				String portKind = portToMoveUp.getKind();

				// Switch Port into Actor Object
				switch (portKind) {
				case PiIdentifiers.DATA_INPUT_PORT:
					portToMoveUpIndex = actor.getDataInputPorts().indexOf(
							portToMoveUp);
					portToMoveDown = actor.getDataInputPorts().get(
							portToMoveUpIndex - 1);
					break;
				case PiIdentifiers.DATA_OUTPUT_PORT:
					portToMoveUpIndex = actor.getDataOutputPorts().indexOf(
							portToMoveUp);
					portToMoveDown = actor.getDataOutputPorts().get(
							portToMoveUpIndex - 1);
					break;
				case PiIdentifiers.CONFIGURATION_INPUT_PORT:
					portToMoveUpIndex = actor.getConfigInputPorts().indexOf(
							portToMoveUp);
					portToMoveDown = actor.getConfigInputPorts().get(
							portToMoveUpIndex - 1);
					break;
				case PiIdentifiers.CONFIGURATION_OUPUT_PORT:
					portToMoveUpIndex = actor.getConfigOutputPorts().indexOf(
							portToMoveUp);
					portToMoveDown = actor.getConfigOutputPorts().get(
							portToMoveUpIndex - 1);
					break;
				}
				portToMoveDownIndex = portToMoveUpIndex - 1;

				// Switch Graphical Elements
				int anchorToMoveUpIndex, anchorToMoveDownIndex = -1;
				ContainerShape CsActor = (ContainerShape) ((BoxRelativeAnchor) pes[0])
						.getReferencedGraphicsAlgorithm().getPictogramElement();
				EList<Anchor> anchors = CsActor.getAnchors();

				anchorToMoveUpIndex = anchors.indexOf(pes[0]);

				for (Anchor a : anchors) {
					if (a.getLink().getBusinessObjects().get(0)
							.equals(portToMoveDown)) {
						anchorToMoveDownIndex = anchors.indexOf(a);
						break;
					}
				}

				if (anchorToMoveDownIndex == -1)
					return;

				// Do Modifications
				this.hasDoneChanges = true;

				switch (portKind) {
				case PiIdentifiers.DATA_INPUT_PORT:
					actor.getDataInputPorts().move(portToMoveDownIndex,
							portToMoveUpIndex);
					break;
				case PiIdentifiers.DATA_OUTPUT_PORT:
					actor.getDataOutputPorts().move(portToMoveDownIndex,
							portToMoveUpIndex);
					break;
				case PiIdentifiers.CONFIGURATION_INPUT_PORT:
					actor.getConfigInputPorts().move(portToMoveDownIndex,
							portToMoveUpIndex);
					break;
				case PiIdentifiers.CONFIGURATION_OUPUT_PORT:
					actor.getConfigOutputPorts().move(portToMoveDownIndex,
							portToMoveUpIndex);
					break;
				}
				anchors.move(anchorToMoveDownIndex, anchorToMoveUpIndex);

				// Layout the Port
				layoutPictogramElement(pes[0]);
				updatePictogramElement(pes[0]);

				// Layout the actor
				layoutPictogramElement(CsActor);
				updatePictogramElement(CsActor);

				// Layout the FIFOs / Dependencies connected to these ports (if
				// any)
				EObject edge = null;
				switch (portToMoveUp.getKind()) {
				case PiIdentifiers.DATA_INPUT_PORT:
					edge = ((DataInputPort) portToMoveUp).getIncomingFifo();
					break;

				case PiIdentifiers.DATA_OUTPUT_PORT:
					edge = ((DataOutputPort) portToMoveUp).getOutgoingFifo();
					break;

				case PiIdentifiers.CONFIGURATION_INPUT_PORT:
					edge = ((ConfigInputPort) portToMoveUp)
							.getIncomingDependency();
					break;

				case PiIdentifiers.CONFIGURATION_OUPUT_PORT:
					EList<Dependency> outgoingDependencies = ((ConfigOutputPort) portToMoveUp)
							.getOutgoingDependencies();
					if (outgoingDependencies.size() > 0) {
						edge = outgoingDependencies.get(0);
					} else {
						edge = null;
					}
					break;
				}
				
				// If the port is connected to an edge
				if(edge != null){
					
				}
			}
		}
	}

	@Override
	public boolean hasDoneChanges() {
		return this.hasDoneChanges;
	}

}
