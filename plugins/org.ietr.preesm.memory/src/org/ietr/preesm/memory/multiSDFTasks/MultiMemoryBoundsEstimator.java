/*******************************************************************************
 * Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
 * Karol Desnos, Julien Heulot, Clément Guy
 * 
 * [mpelcat,jnezan,kdesnos,jheulot,cguy]@insa-rennes.fr
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
package org.ietr.preesm.memory.multiSDFTasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ietr.dftools.algorithm.model.dag.DirectedAcyclicGraph;
import org.ietr.dftools.workflow.WorkflowException;
import org.ietr.dftools.workflow.elements.Workflow;
import org.ietr.dftools.workflow.tools.WorkflowLogger;
import org.ietr.preesm.memory.bounds.AbstractMemoryBoundsEstimator;
import org.ietr.preesm.memory.bounds.MemoryBoundsEstimatorEngine;
import org.ietr.preesm.memory.exclusiongraph.MemoryExclusionGraph;

public class MultiMemoryBoundsEstimator extends AbstractMemoryBoundsEstimator {

	@Override
	public Map<String, Object> execute(Map<String, Object> inputs,
			Map<String, String> parameters, IProgressMonitor monitor,
			String nodeName, Workflow workflow) throws WorkflowException {

		// Rem: Logger is used to display messages in the console
		Logger logger = WorkflowLogger.getLogger();

		// Check Workflow element parameters
		String valueVerbose = parameters.get(PARAM_VERBOSE);
		String valueSolver = parameters.get(PARAM_SOLVER);

		@SuppressWarnings("unchecked")
		Map<DirectedAcyclicGraph, MemoryExclusionGraph> dagsAndMemExs = (Map<DirectedAcyclicGraph, MemoryExclusionGraph>) inputs
				.get(KEY_DAG_AND_MEM_EX_MAP);

		Set<Integer> minBounds = new HashSet<Integer>();
		Set<Integer> maxBounds = new HashSet<Integer>();
		
		for (MemoryExclusionGraph memEx : dagsAndMemExs.values()) {
			MemoryBoundsEstimatorEngine engine = new MemoryBoundsEstimatorEngine(
					memEx, valueVerbose);
			engine.selectSolver(valueSolver);
			engine.solve();

			int minBound = engine.getMinBound();

			int maxBound = engine.getMaxBound();

			logger.log(Level.INFO, "Bound_Max = " + maxBound + " Bound_Min = "
					+ minBound);
			System.out.println(minBound + ";");
			
			minBounds.add(minBound);
			maxBounds.add(maxBound);
		}
		// Generate output
		Map<String, Object> output = new HashMap<String, Object>();
		output.put(KEY_BOUND_MAX_SET, maxBounds);
		output.put(KEY_BOUND_MIN_SET, minBounds);
		output.put(KEY_DAG_AND_MEM_EX_MAP, dagsAndMemExs);
		return output;
	}

	@Override
	public Map<String, String> getDefaultParameters() {
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put(PARAM_SOLVER, VALUE_SOLVER_DEFAULT);
		parameters.put(PARAM_VERBOSE, VALUE_VERBOSE_DEFAULT);
		return parameters;
	}

	@Override
	public String monitorMessage() {
		return "Estimating Memory Bounds";
	}

}
