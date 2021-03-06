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

import java.util.List;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.ietr.preesm.experiment.model.pimm.Fifo;

/**
 * Delete feature to remove a {@link Delay} from a {@link Fifo}
 * 
 * @author kdesnos
 * 
 */
public class DeleteDelayFeature extends DeleteParameterizableFeature {

	/**
	 * Default Constructor of the {@link DeleteDelayFeature}
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public DeleteDelayFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public void preDelete(IDeleteContext context) {
		// Transform the two connections linked to the delay back into a single
		// one. before deleting the delay.

		// Retrieve the two connections
		ContainerShape cs = (ContainerShape) context.getPictogramElement();
		ChopboxAnchor cba = (ChopboxAnchor) cs.getAnchors().get(0);
		List<Connection> incomingConnections = cba.getIncomingConnections();
		// There can be dependency incoming connection. Find the unique fifo
		// incoming connection
		Connection preConnection = null;
		for (Connection connection : incomingConnections) {
			Object obj = getBusinessObjectForPictogramElement(connection);
			if (obj instanceof Fifo) {
				preConnection = connection;
				break;
			}
		}
		// There is only one outgoing connection, the Fifo one.
		Connection postConnection = cba.getOutgoingConnections().get(0);

		// Copy the bendpoints to the unique remaining connection.
		// Reconnect it.
		((FreeFormConnection) postConnection).getBendpoints().addAll(0,
				((FreeFormConnection) preConnection).getBendpoints());
		postConnection.setStart(preConnection.getStart());

		// Remove the preConnection (but not the associated Fifo)
		IRemoveContext rmCtxt = new RemoveContext(preConnection);
		IRemoveFeature rmFeature = this.getFeatureProvider().getRemoveFeature(
				rmCtxt);
		if (rmFeature.canRemove(rmCtxt)) {
			rmFeature.remove(rmCtxt);
		} else {
			throw new RuntimeException(
					"Could not delete Delay because a Connection could not be removed.");
		}

		// Super call to delete the dependencies linked to the delay
		super.preDelete(context);
	}

}
