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


package org.ietr.preesm.plugin.mapper.model;

import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.Medium;
import org.ietr.preesm.core.architecture.Operator;

/**
 * Properties of an implanted vertex
 * 
 * @author mpelcat
 */
public class ImplementationVertexProperty {

	/**
	 * Operator to which the vertex has been affected by the mapping algorithm
	 */
	private ArchitectureComponent effectiveComponent;

	/**
	 * The order in the schedule of a processor is determined by the order of
	 * the calls to implant() method.
	 */
	private int schedulingTotalOrder;

	public ImplementationVertexProperty() {
		super();
		effectiveComponent = Operator.NO_COMPONENT;

		schedulingTotalOrder = -1;
	}

	@Override
	public ImplementationVertexProperty clone() {

		ImplementationVertexProperty property = new ImplementationVertexProperty();
		property.setEffectiveComponent(this.getEffectiveComponent());
		property.setSchedulingTotalOrder(this.schedulingTotalOrder);
		return property;
	}

	/**
	 * A computation vertex has an effective operator
	 */
	public Operator getEffectiveOperator() {
		if (effectiveComponent instanceof Operator)
			return (Operator) effectiveComponent;
		else
			return (Operator) Operator.NO_COMPONENT;
	}

	public boolean hasEffectiveOperator() {
		return getEffectiveOperator() != Operator.NO_COMPONENT;
	}

	public void setEffectiveOperator(Operator effectiveOperator) {
		this.effectiveComponent = effectiveOperator;
	}

	/**
	 * A Communication vertex has an effective medium
	 */
	public Medium getEffectiveMedium() {
		if (effectiveComponent instanceof Medium)
			return (Medium) effectiveComponent;
		else
			return (Medium) Medium.NO_COMPONENT;
	}

	public boolean hasEffectiveMedium() {
		return getEffectiveMedium() != Operator.NO_COMPONENT;
	}

	public void setEffectiveMedium(Medium effectiveMedium) {
		this.effectiveComponent = effectiveMedium;
	}

	/**
	 * Effective component is common to communication and computation vertices
	 */
	public ArchitectureComponent getEffectiveComponent() {
		return effectiveComponent;
	}

	public boolean hasEffectiveComponent() {
		return getEffectiveComponent() != ArchitectureComponent.NO_COMPONENT;
	}

	public void setEffectiveComponent(ArchitectureComponent component) {
		this.effectiveComponent = component;
	}

	public int getSchedulingTotalOrder() {
		return schedulingTotalOrder;
	}

	public void setSchedulingTotalOrder(int schedulingTotalOrder) {
		this.schedulingTotalOrder = schedulingTotalOrder;
	}

}
