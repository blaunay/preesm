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
package org.ietr.preesm.experiment.model.pimm;

import org.eclipse.emf.common.util.EList;
import org.ietr.preesm.experiment.model.pimm.util.PiMMVisitable;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Abstract Vertex</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.AbstractActor#getDataInputPorts <em>Data Input Ports</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.AbstractActor#getDataOutputPorts <em>Data Output Ports</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.AbstractActor#getConfigOutputPorts <em>Config Output Ports</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getAbstractActor()
 * @model abstract="true"
 * @generated
 */
public interface AbstractActor extends AbstractVertex, PiMMVisitable {
	/**
	 * Returns the value of the '<em><b>Data Input Ports</b></em>' containment reference list.
	 * The list contents are of type {@link org.ietr.preesm.experiment.model.pimm.DataInputPort}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Input Ports</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Input Ports</em>' containment reference list.
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getAbstractActor_DataInputPorts()
	 * @model containment="true"
	 * @generated
	 */
	EList<DataInputPort> getDataInputPorts();

	/**
	 * Returns the value of the '<em><b>Data Output Ports</b></em>' containment reference list.
	 * The list contents are of type {@link org.ietr.preesm.experiment.model.pimm.DataOutputPort}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Output Ports</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Output Ports</em>' containment reference list.
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getAbstractActor_DataOutputPorts()
	 * @model containment="true"
	 * @generated
	 */
	EList<DataOutputPort> getDataOutputPorts();

	/**
	 * Returns the value of the '<em><b>Config Output Ports</b></em>' containment reference list.
	 * The list contents are of type {@link org.ietr.preesm.experiment.model.pimm.ConfigOutputPort}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Config Output Ports</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Config Output Ports</em>' containment reference list.
	 * @see org.ietr.preesm.experiment.model.pimm.PiMMPackage#getAbstractActor_ConfigOutputPorts()
	 * @model containment="true"
	 * @generated
	 */
	EList<ConfigOutputPort> getConfigOutputPorts();

	public String getPath();

} // AbstractActor
