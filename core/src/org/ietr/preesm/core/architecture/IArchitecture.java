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


/**
 * 
 */
package org.ietr.preesm.core.architecture;

import java.util.Set;

/**
 * This interface provides methods to manipulate an architecture.
 * 
 * @author Matthieu Wipliez
 */
public interface IArchitecture {

	public IArchitecture clone();

	/**
	 * Returns the main medium definition. This definition is used by kwok
	 * homogeneous architecture algorithm to pre-study the implementation
	 */
	public Medium getMainMedium();

	/**
	 * Returns the main operator. This definition is used by kwok homogeneous
	 * architecture algorithm to pre-study the implementation
	 */
	public Operator getMainOperator();

	/**
	 * Returns all the media
	 * 
	 * @return
	 */
	public Set<Medium> getMedia();

	/**
	 * Returns the operator of the given id
	 * 
	 * @param op1
	 * @param op2
	 * @return
	 */
	public Set<Medium> getMedia(Operator op1, Operator op2);

	public Medium getMedium(String name);

	public MediumDefinition getMediumDefinition(String id);

	/**
	 * Returns the number of operators.
	 * 
	 * @return The number of operators.
	 */
	public int getNumberOfOperators();

	/**
	 * Returns the operator of the given id
	 * 
	 * @param id
	 * @return
	 */
	public Operator getOperator(String id);

	/**
	 * Returns the operator definition with the given id
	 * 
	 * @param id
	 * @return
	 */
	public OperatorDefinition getOperatorDefinition(String id);

	/**
	 * Returns all the operators
	 * 
	 * @return
	 */
	public Set<Operator> getOperators();

	/**
	 * Returns the operators of the given type
	 * 
	 * @param def
	 * @return
	 */
	public Set<Operator> getOperators(OperatorDefinition def);

	/**
	 * Returns the name of the architecture
	 */
	public String getName();
}
