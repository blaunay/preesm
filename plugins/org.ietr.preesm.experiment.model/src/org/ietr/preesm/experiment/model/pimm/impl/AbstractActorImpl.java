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
package org.ietr.preesm.experiment.model.pimm.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.ietr.preesm.experiment.model.pimm.AbstractActor;
import org.ietr.preesm.experiment.model.pimm.ConfigOutputPort;
import org.ietr.preesm.experiment.model.pimm.InputPort;
import org.ietr.preesm.experiment.model.pimm.OutputPort;
import org.ietr.preesm.experiment.model.pimm.PiMMPackage;
import org.ietr.preesm.experiment.model.pimm.Port;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Abstract Vertex</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.AbstractActorImpl#getInputPorts <em>Input Ports</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.AbstractActorImpl#getOutputPorts <em>Output Ports</em>}</li>
 *   <li>{@link org.ietr.preesm.experiment.model.pimm.impl.AbstractActorImpl#getConfigOutputPorts <em>Config Output Ports</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class AbstractActorImpl extends AbstractVertexImpl implements
		AbstractActor {
	/**
	 * The cached value of the '{@link #getInputPorts() <em>Input Ports</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getInputPorts()
	 * @generated
	 * @ordered
	 */
	protected EList<InputPort> inputPorts;

	/**
	 * The cached value of the '{@link #getOutputPorts() <em>Output Ports</em>}' containment reference list.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @see #getOutputPorts()
	 * @generated
	 * @ordered
	 */
	protected EList<OutputPort> outputPorts;

	/**
	 * The cached value of the '{@link #getConfigOutputPorts() <em>Config Output Ports</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConfigOutputPorts()
	 * @generated
	 * @ordered
	 */
	protected EList<ConfigOutputPort> configOutputPorts;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	protected AbstractActorImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PiMMPackage.ABSTRACT_ACTOR__INPUT_PORTS:
				return getInputPorts();
			case PiMMPackage.ABSTRACT_ACTOR__OUTPUT_PORTS:
				return getOutputPorts();
			case PiMMPackage.ABSTRACT_ACTOR__CONFIG_OUTPUT_PORTS:
				return getConfigOutputPorts();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd,
			int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PiMMPackage.ABSTRACT_ACTOR__INPUT_PORTS:
				return ((InternalEList<?>)getInputPorts()).basicRemove(otherEnd, msgs);
			case PiMMPackage.ABSTRACT_ACTOR__OUTPUT_PORTS:
				return ((InternalEList<?>)getOutputPorts()).basicRemove(otherEnd, msgs);
			case PiMMPackage.ABSTRACT_ACTOR__CONFIG_OUTPUT_PORTS:
				return ((InternalEList<?>)getConfigOutputPorts()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case PiMMPackage.ABSTRACT_ACTOR__INPUT_PORTS:
				return inputPorts != null && !inputPorts.isEmpty();
			case PiMMPackage.ABSTRACT_ACTOR__OUTPUT_PORTS:
				return outputPorts != null && !outputPorts.isEmpty();
			case PiMMPackage.ABSTRACT_ACTOR__CONFIG_OUTPUT_PORTS:
				return configOutputPorts != null && !configOutputPorts.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PiMMPackage.ABSTRACT_ACTOR__INPUT_PORTS:
				getInputPorts().clear();
				getInputPorts().addAll((Collection<? extends InputPort>)newValue);
				return;
			case PiMMPackage.ABSTRACT_ACTOR__OUTPUT_PORTS:
				getOutputPorts().clear();
				getOutputPorts().addAll((Collection<? extends OutputPort>)newValue);
				return;
			case PiMMPackage.ABSTRACT_ACTOR__CONFIG_OUTPUT_PORTS:
				getConfigOutputPorts().clear();
				getConfigOutputPorts().addAll((Collection<? extends ConfigOutputPort>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PiMMPackage.Literals.ABSTRACT_ACTOR;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case PiMMPackage.ABSTRACT_ACTOR__INPUT_PORTS:
				getInputPorts().clear();
				return;
			case PiMMPackage.ABSTRACT_ACTOR__OUTPUT_PORTS:
				getOutputPorts().clear();
				return;
			case PiMMPackage.ABSTRACT_ACTOR__CONFIG_OUTPUT_PORTS:
				getConfigOutputPorts().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EList<InputPort> getInputPorts() {
		if (inputPorts == null) {
			inputPorts = new EObjectContainmentEList<InputPort>(InputPort.class, this, PiMMPackage.ABSTRACT_ACTOR__INPUT_PORTS);
		}
		return inputPorts;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EList<OutputPort> getOutputPorts() {
		if (outputPorts == null) {
			outputPorts = new EObjectContainmentEList<OutputPort>(OutputPort.class, this, PiMMPackage.ABSTRACT_ACTOR__OUTPUT_PORTS);
		}
		return outputPorts;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ConfigOutputPort> getConfigOutputPorts() {
		if (configOutputPorts == null) {
			configOutputPorts = new EObjectContainmentEList<ConfigOutputPort>(ConfigOutputPort.class, this, PiMMPackage.ABSTRACT_ACTOR__CONFIG_OUTPUT_PORTS);
		}
		return configOutputPorts;
	}

	@Override
	public Port getPortNamed(String portName) {
		// If the super method return a port, return it
		Port p = super.getPortNamed(portName);
		if (p != null) {
			return p;
		}

		List<Port> ports = new ArrayList<Port>(getInputPorts());

		ports.addAll(getOutputPorts());
		ports.addAll(getConfigOutputPorts());

		for (Object port : ports) {
			String name = ((Port) port).getName();
			if (name == null && portName == null) {
				return (Port) port;
			}
			if (name != null && name.equals(portName)) {
				return (Port) port;
			}
		}
		return null;
	}

} // AbstractActoImpl
