/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

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

package org.ietr.preesm.core.scenario;

import java.util.ArrayList;
import java.util.List;

import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.scenario.serialize.ExcelTimingParser;
import org.sdf4j.model.dag.DAGVertex;
import org.sdf4j.model.parameters.InvalidExpressionException;
import org.sdf4j.model.sdf.SDFAbstractVertex;
import org.sdf4j.model.sdf.SDFGraph;

/**
 * Manager of the graphs timings
 * 
 * @author mpelcat
 * 
 */
public class TimingManager {

	/**
	 * Default timing when none was set
	 */
	private Timing defaultTiming = null;

	/**
	 * List of all timings
	 */
	private List<Timing> timings;

	/**
	 * Path to a file containing timings
	 */
	private String excelFileURL = "";

	public TimingManager() {
		timings = new ArrayList<Timing>();
		defaultTiming = new Timing("default", "default",
				Timing.DEFAULT_TASK_TIME);
	}

	public Timing addTiming(String sdfVertexId, String operatorDefinitionId) {

		Timing newt = new Timing(operatorDefinitionId, sdfVertexId);
		for (Timing timing : timings) {
			if (timing.equals(newt)) {
				return timing;
			}
		}

		timings.add(newt);
		return newt;
	}

	public void setTiming(String sdfVertexId, String operatorDefinitionId,
			int time) {

		addTiming(sdfVertexId, operatorDefinitionId).setTime(time);
	}

	public Timing addTiming(Timing newt) {

		for (Timing timing : timings) {
			if (timing.equals(newt)) {
				timing.setTime(newt.getTime());
				return timing;
			}
		}

		timings.add(newt);
		return newt;
	}

	public List<Timing> getGraphTimings(DAGVertex dagVertex,
			MultiCoreArchitecture architecture) {
		SDFAbstractVertex sdfVertex = dagVertex.getCorrespondingSDFVertex();
		List<Timing> vals = new ArrayList<Timing>();

		if (sdfVertex.getGraphDescription() == null) {
			for (Timing timing : timings) {
				if (timing.getSdfVertexId().equals(sdfVertex.getName())) {
					vals.add(timing);
				}
			}
		} else if (sdfVertex.getGraphDescription() instanceof SDFGraph) {
			// Adds timings for all operators in hierarchy if they can be
			// calculated
			// from underlying vertices
			for (String opDefId : architecture.getOperatorDefinitionIds()) {
				Timing t = generateVertexTimingFromHierarchy(
						dagVertex.getCorrespondingSDFVertex(), opDefId);
				if (t != null)
					vals.add(t);
			}
		}

		return vals;
	}

	private Timing getVertexTiming(SDFAbstractVertex sdfVertex, String opDefId) {
		for (Timing timing : timings) {
			if (timing.getSdfVertexId().equals(sdfVertex.getName())
					&& timing.getOperatorDefinitionId().equals(opDefId)) {
				return timing;
			}
		}
		return null;
	}

	/**
	 * Calculates a vertex timing from its underlying vertices
	 */
	public Timing generateVertexTimingFromHierarchy(
			SDFAbstractVertex sdfVertex, String opDefId) {
		int maxTime = 0;
		SDFGraph graphDescription = (SDFGraph) sdfVertex.getGraphDescription();

		for (SDFAbstractVertex vertex : graphDescription.vertexSet()) {
			Timing vertexTiming;
			if (vertex.getGraphDescription() != null) {
				maxTime += generateVertexTimingFromHierarchy(vertex, opDefId)
						.getTime();
			} else if ((vertexTiming = getVertexTiming(vertex, opDefId)) != null) {
				try {
					maxTime += vertexTiming.getTime()
							* vertex.getNbRepeatAsInteger();
				} catch (InvalidExpressionException e) {
					maxTime += vertexTiming.getTime();
				}
			}
			if (maxTime < 0) {
				maxTime = Integer.MAX_VALUE;
				break;
			}
		}
		// TODO: time calculation for underlying tasks not ready
		return (new Timing(opDefId, sdfVertex.getName(), maxTime));

		/*
		 * SDFGraph graph = (SDFGraph)sdfVertex.getGraphDescription();
		 * 
		 * int time = 0; for(SDFAbstractVertex v : graph.vertexSet()){
		 * if(sdfVertex.getGraphDescription() == null){ time += sdfVertex. } }
		 * 
		 * if(time>=0) return(new Timing(opDef,sdfVertex,time)); else return
		 * null;
		 */
	}

	/**
	 * Looks for a timing entered in scenario editor. If there is none, returns
	 * a default value
	 */
	public int getTimingOrDefault(String sdfVertexId,
			String operatorDefinitionId) {
		Timing val = null;

		for (Timing timing : timings) {
			if (timing.getSdfVertexId().equals(sdfVertexId)
					&& timing.getOperatorDefinitionId().equals(
							operatorDefinitionId)) {
				val = timing;
			}
		}

		if (val == null) {
			val = defaultTiming;
		}

		return val.getTime();
	}

	public List<Timing> getTimings() {

		return timings;
	}

	public void removeAll() {

		timings.clear();
	}

	public String getExcelFileURL() {
		return excelFileURL;
	}

	public void setExcelFileURL(String excelFileURL) {
		this.excelFileURL = excelFileURL;
	}

	public void importTimings(PreesmScenario currentScenario) {
		if (!excelFileURL.isEmpty() && currentScenario != null) {
			ExcelTimingParser parser = new ExcelTimingParser(currentScenario);
			parser.parse(excelFileURL);
		}
	}
}
