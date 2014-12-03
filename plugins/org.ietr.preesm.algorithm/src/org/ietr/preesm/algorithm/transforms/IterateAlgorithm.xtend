/**
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
 */
package org.ietr.preesm.algorithm.transforms

import java.util.HashMap
import java.util.Map
import org.eclipse.core.runtime.IProgressMonitor
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex
import org.ietr.dftools.algorithm.model.sdf.SDFGraph
import org.ietr.dftools.workflow.WorkflowException
import org.ietr.dftools.workflow.elements.Workflow
import org.ietr.dftools.workflow.implement.AbstractTaskImplementation

/**
 * Repeating N times the same single rate IBSDF algorithm into a new IBSDF graph.
 * A delayed edge of weight 1 is introduced between 2 iterations of the same actor
 * to ensure precedence.
 * 
 * @author mpelcat
 * 
 */
class IterateAlgorithm extends AbstractTaskImplementation {

	/**
	 * Tag for storing the requested number of iterations
	 */
	val NB_IT = "nbIt";	
	
	/**
	 * Mixing several iterations of a single graph
	 */
	def iterate(SDFGraph inputAlgorithm, int nbIt) {
		// Generating a first graph clone
		var mainIteration = inputAlgorithm.clone
		
		// Incorporating new iterations
		for (Integer i: 1..nbIt-1)  {
			// Adding vertices of new_iteration to the ones of mainIteration, vertices are automatically renamed
			mainIteration.fill(inputAlgorithm)
    	}

		return mainIteration
	
	}

	/**
	 * Executing the workflow element
	 */
	override execute(Map<String, Object> inputs, Map<String, String> parameters, IProgressMonitor monitor,
		String nodeName, Workflow workflow) throws WorkflowException {
		val outMap = new HashMap<String, Object>
		val inputAlgorithm = inputs.get("SDF") as SDFGraph
		val nbIt = Integer.valueOf(parameters.get(NB_IT))
		val outputAlgorithm = iterate(inputAlgorithm, nbIt)
		outMap.put("SDF", outputAlgorithm)
		return outMap;
	}

	/**
	 * If no parameter is set, using default value
	 */
	override getDefaultParameters() {
		val defaultParameters = new HashMap<String, String>
		defaultParameters.put(NB_IT, "1")
		return defaultParameters
	}

	/**
	 * Message displayed in the DFTools console
	 */
	override monitorMessage() '''
    Iterating a single rate IBSDF «defaultParameters.get(NB_IT)» time(s).'''

}
