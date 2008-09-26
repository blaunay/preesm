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
package org.ietr.preesm.core.codegen;

import java.util.ArrayList;
import java.util.Iterator;

import org.ietr.preesm.core.codegen.sdfProperties.BufferAggregate;

/**
 * Container that handles the semaphores
 * 
 * @author mpelcat
 */
public class SemaphoreContainer extends ArrayList<Semaphore> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Buffer container containing the current semaphore manager
	 */
	AbstractBufferContainer parentContainer;

	public SemaphoreContainer(AbstractBufferContainer parentContainer) {
		super();
		this.parentContainer = parentContainer;
	}

	public void allocateSemaphores(){
		
		Buffer buf = new Buffer("sem", this.size(),new DataType("semaphore"),null);
		
		parentContainer.addBuffer(new BufferAllocation(buf));
	}

	public Semaphore createSemaphore(BufferAggregate agg, SemaphoreType type) {

		Semaphore sem = getSemaphore(agg, type);

		if (sem == null) {
			sem = new Semaphore(this, agg, type);
			add(sem);
			return sem;
		} else {
			return sem;
		}
	}
	
	
	public Semaphore getSemaphore(BufferAggregate agg, SemaphoreType type) {

		Semaphore sem = null;

		Iterator<Semaphore> currentIt = iterator();

		while (currentIt.hasNext()) {
			sem = currentIt.next();

			// Two semaphores protecting the same buffer in the same direction
			// are the same semaphore
			if (sem.getProtectedBuffers() == agg
					&& sem.getSemaphoreType() == type) {
				return sem;
			}
		}
		
		return null;
	}
}
