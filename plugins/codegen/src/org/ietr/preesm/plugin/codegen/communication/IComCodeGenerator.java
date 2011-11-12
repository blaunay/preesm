/**
 * 
 */
package org.ietr.preesm.plugin.codegen.communication;

import net.sf.dftools.algorithm.model.sdf.SDFAbstractVertex;

/**
 * Generating communication code (initialization and calls) for a given type of
 * Route Step
 * 
 * @author mpelcat
 */
public interface IComCodeGenerator {
	public void createComs(SDFAbstractVertex vertex);
}
