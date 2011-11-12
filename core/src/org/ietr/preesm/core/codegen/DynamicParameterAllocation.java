package org.ietr.preesm.core.codegen;

import org.ietr.preesm.core.codegen.calls.Variable;
import net.sf.dftools.algorithm.model.psdf.parameters.PSDFDynamicParameter;

public class DynamicParameterAllocation extends VariableAllocation {

	PSDFDynamicParameter parameter;

	public DynamicParameterAllocation(PSDFDynamicParameter param, Variable var) {
		super(var);
		parameter = param;
	}
}
