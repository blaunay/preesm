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

import org.ietr.preesm.core.architecture.IArchitecture;
import org.ietr.preesm.core.architecture.Operator;
import org.sdf4j.model.dag.DirectedAcyclicGraph;

/**
 * A generated code is the gathering of source files, each
 * one corresponding to one core.
 * 
 * @author mwipliez
 * @author mpelcat
 */
public class SourceFileList extends ArrayList<SourceFile> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The main source file.
	 */
	private SourceFile main;

	/**
	 * 
	 */
	public SourceFileList() {
	}

	/**
	 * Creates the source files from an architecture
	 */
	private void createSourceFiles(IArchitecture architecture) {

		Iterator<Operator> iterator = architecture.getOperators().iterator();
		
		// Generates and populates one source file per core
		while(iterator.hasNext()){
			
			Operator currentOp = iterator.next();
			
			SourceFile sourceFile = new SourceFile(currentOp.getName(), currentOp);
			add(sourceFile);
			
			// The main operator leads to the main source file
			if(architecture.getMainOperator().equals(currentOp)){
				setMain(sourceFile);
			}
		}
	}

	/**
	 * Creates and fills source files from an SDF and an architecture
	 */
	public void generateSourceFiles(DirectedAcyclicGraph algorithm, IArchitecture architecture) {

		// Creates one source file per operator
		createSourceFiles(architecture);
		
		Iterator<SourceFile> iterator = this.iterator();

		// For each source file, generates source code
		while(iterator.hasNext()){
			
			SourceFile currentSource = iterator.next();
			
			currentSource.generateSource(algorithm, architecture);
		}
	}
	
	/**
	 * Returns the main source file.
	 * 
	 * @return
	 */
	public SourceFile getMain() {
		return main;
	}
	
	/**
	 * Sets the given source file as being the main file. If the source file is
	 * not part of the source file list, it will be added.
	 * 
	 * @param main
	 *            A source file.
	 */
	public void setMain(SourceFile main) {
		if (!contains(main)) {
			add(main);
		}

		this.main = main;
	}
	
	/**
	 * Displays pseudo-code for test
	 */
	public String toString() {
		String code = "";
		
		Iterator<SourceFile> iterator = iterator();
		
		// Displays every files
		while(iterator.hasNext()){
			SourceFile file = iterator.next();
			
			code += file.toString();

			code += "\n//---------------------------\n";
		}
		
		return code;
	}
}
