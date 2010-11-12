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

package org.ietr.preesm.core.workflow.sources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.ietr.preesm.core.architecture.ArchitectureComponent;
import org.ietr.preesm.core.architecture.MultiCoreArchitecture;
import org.ietr.preesm.core.architecture.parser.DesignParser;

/**
 * Class used while retrieving the architecture at the beginning of the workflow
 * execution
 * 
 * @author mpelcat
 */
public class ArchitectureRetriever {

	MultiCoreArchitecture architecture = null;

	public ArchitectureRetriever(String architecturePath) {
		super();

		String filename = architecturePath;
		DesignParser parser = new DesignParser();

		Path relativePath = new Path(filename);
		IFile file = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(relativePath);

		architecture = parser.parseXmlFile(file);

		addVertexPathProperties(architecture, "");
	}

	public MultiCoreArchitecture getArchitecture() {
		return architecture;
	}

	/**
	 * Adding an information that keeps the path of each vertex relative to the
	 * hierarchy
	 */
	private void addVertexPathProperties(MultiCoreArchitecture architecture,
			String currentPath) {

		for (ArchitectureComponent vertex : architecture.vertexSet()) {
			String newPath = currentPath + vertex.getName();
			vertex.setInfo(newPath);
			newPath += "/";
			if (vertex.getGraphDescription() != null) {
				addVertexPathProperties(
						(MultiCoreArchitecture) vertex.getGraphDescription(),
						newPath);
			}
		}
	}
}
