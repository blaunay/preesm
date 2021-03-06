/*********************************************************
Copyright or © or Copr. IETR/INSA: Maxime Pelcat, Jean-François Nezan,
Karol Desnos

[mpelcat,jnezan,kdesnos]@insa-rennes.fr

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

package org.ietr.preesm.ui.scenario.editor.variables;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.ietr.dftools.algorithm.importer.InvalidModelException;
import org.ietr.dftools.algorithm.model.parameters.InvalidExpressionException;
import org.ietr.dftools.algorithm.model.parameters.NoIntegerValueException;
import org.ietr.dftools.algorithm.model.parameters.Variable;
import org.ietr.dftools.algorithm.model.parameters.VariableSet;
import org.ietr.dftools.algorithm.model.sdf.SDFGraph;
import org.ietr.preesm.core.scenario.PreesmScenario;
import org.ietr.preesm.core.scenario.serialize.ScenarioParser;
import org.ietr.preesm.ui.scenario.editor.ExcelWriter;
import org.ietr.preesm.ui.scenario.editor.SaveAsWizard;

/**
 * Exporting timings in an excel sheet
 * 
 * @author mpelcat
 * @author kdesnos
 */
public class ExcelVariablesWriter extends ExcelWriter {

	private PreesmScenario scenario;

	public ExcelVariablesWriter(PreesmScenario scenario) {
		super();
		this.scenario = scenario;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		SaveAsWizard wizard = new SaveAsWizard(this, "Variables");
		WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
		dialog.open();
	}

	/**
	 * Add timing cells to the newly created file
	 * 
	 * @throws IOException
	 */
	@Override
	public void write(OutputStream os) {

		try {
			WorkbookSettings ws = new WorkbookSettings();
			ws.setLocale(new Locale("en", "EN"));
			WritableWorkbook workbook = Workbook.createWorkbook(os, ws);
			WritableSheet sheet = workbook.createSheet("Variables", 0);

			addCells(sheet);
			workbook.write();
			workbook.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void addCells(WritableSheet sheet) throws InvalidModelException,
			FileNotFoundException, RowsExceededException {
		if (sheet != null) {

			int maxVOrdinate = 0;

			// Get variables of the graph
			SDFGraph currentGraph = ScenarioParser.getSDFGraph(scenario
					.getAlgorithmURL());
			VariableSet variablesGraph = currentGraph.getVariables();

			// Get variables of the scenario
			VariableSet variablesScenario = scenario.getVariablesManager()
					.getVariables();
			;

			for (String variableName : variablesGraph.keySet()) {
				// Retrieve the cell (if it exists)
				WritableCell varCell = (WritableCell) sheet
						.findCell(variableName);
				try {
					// If the cell does not exists, create it
					if (varCell == null) {
						varCell = new Label(0, maxVOrdinate, variableName);
						sheet.addCell(varCell);
						maxVOrdinate++;
					}

					// Get the variable value from scenario
					// if variable not defined in scenario, get value from graph
					Variable variable = variablesScenario
							.getVariable(variableName);
					if (variable == null) {
						variable = variablesGraph.getVariable(variableName);
					}
					int value = variable.intValue();

					// Write the value in the cell
					WritableCell valueCell = new Number(1, varCell.getRow(),
							value);
					sheet.addCell(valueCell);

				} catch (WriteException | InvalidExpressionException
						| NoIntegerValueException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
