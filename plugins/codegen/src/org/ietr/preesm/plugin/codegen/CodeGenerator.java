/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,jnezan,mraulet]@insa-rennes.fr

This software is a computer program whose purpose is to prototype
parallel applications.

This software is governed by the CeCILL-B license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-B
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
knowledge of the CeCILL-B license and that you accept its terms.
 *********************************************************/

package org.ietr.preesm.plugin.codegen;

import java.util.Iterator;

import net.sf.dftools.architecture.slam.ComponentInstance;
import net.sf.dftools.architecture.slam.Design;

import org.ietr.preesm.core.architecture.util.DesignTools;
import org.ietr.preesm.core.codegen.SourceFile;
import org.ietr.preesm.core.codegen.SourceFileList;
import org.ietr.preesm.core.codegen.model.CodeGenSDFGraph;
import org.ietr.preesm.core.scenario.PreesmScenario;

/**
 * Top-level code generation class. Creates and fills source files
 * 
 * @author Matthieu Wipliez
 * @author mpelcat
 */
public class CodeGenerator {

	/**
	 * A list of source file objects
	 */
	private SourceFileList list;

	public CodeGenerator(SourceFileList list) {
		this.list = list;
	}

	/**
	 * Creates the source files from an architecture
	 */
	private void createSourceFiles(Design architecture, PreesmScenario scenario) {
		Iterator<ComponentInstance> iterator = DesignTools
				.getOperatorInstances(architecture).iterator();

		// Generates and populates one source file per core
		while (iterator.hasNext()) {

			ComponentInstance currentOp = (ComponentInstance) iterator.next();

			SourceFile sourceFile = new SourceFile(currentOp.getInstanceName(),
					currentOp, list);
			list.add(sourceFile);

			// The main operator leads to the main source file
			if (scenario.getSimulationManager().getMainOperatorName()
					.equals(currentOp.getInstanceName())) {
				list.setMain(sourceFile);
			}
		}
	}

	/**
	 * Creates and fills source files from an SDF and an architecture
	 */
	public void generateSourceFiles(CodeGenSDFGraph algorithm,
			Design architecture, PreesmScenario scenario) {
		// Creates one source file per operator
		createSourceFiles(architecture, scenario);

		// For each source file, generates source code
		for (SourceFile file : list) {
			SourceFileCodeGenerator codegen = new SourceFileCodeGenerator(file);
			codegen.generateSource(algorithm, architecture);
		}
	}
}
