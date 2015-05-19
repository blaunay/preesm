/*******************************************************************************
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
 ******************************************************************************/
package org.ietr.preesm.ui.pimm.features;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.ietr.preesm.experiment.model.pimm.Actor;
import org.ietr.preesm.experiment.model.pimm.Direction;
import org.ietr.preesm.experiment.model.pimm.FunctionParameter;
import org.ietr.preesm.experiment.model.pimm.FunctionPrototype;
import org.ietr.preesm.experiment.model.pimm.HRefinement;
import org.ietr.preesm.experiment.model.pimm.PiMMFactory;
import org.ietr.preesm.experiment.model.pimm.Refinement;
import org.ietr.preesm.ui.pimm.util.PiMMUtil;
import org.ietr.preesm.utils.pimm.header.parser.cdt.ASTAndActorComparisonVisitor;

/**
 * Custom Feature to set a new {@link Refinement} of an {@link Actor}
 * 
 * @author kdesnos
 * 
 */
public class SetActorRefinementFeature extends AbstractCustomFeature {

	protected boolean hasDoneChanges = false;

	enum PrototypeFilter {
		NONE, INIT_ACTOR, LOOP_ACTOR, INIT
	}

	/**
	 * Default Constructor of {@link SetActorRefinementFeature}
	 * 
	 * @param fp
	 *            the feature provider
	 */
	public SetActorRefinementFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public String getName() {
		return "Set Refinement";
	}

	@Override
	public String getDescription() {
		return "Set/Change the Refinement of an Actor";
	}

	@Override
	public boolean canExecute(ICustomContext context) {
		// Allow setting if exactly one pictogram element
		// representing an Actor is selected
		boolean ret = false;
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof Actor) {
				ret = true;
			}
		}
		return ret;
	}

	@Override
	public void execute(ICustomContext context) {

		// Re-check if only one element is selected
		PictogramElement[] pes = context.getPictogramElements();
		if (pes != null && pes.length == 1) {
			Object bo = getBusinessObjectForPictogramElement(pes[0]);
			if (bo instanceof Actor) {
				Actor actor = (Actor) bo;

				String question = "Please select a valid file\n(.idl, .h or .pi)";
				String dialogTitle = "Select a refinement file";
				IPath path = askRefinement(actor, question, dialogTitle);
				if (path != null) {
					setActorRefinement(actor, path);
				}

				// Call the layout feature
				layoutPictogramElement(pes[0]);
			}
		}
	}

	private IPath askRefinement(Actor actor, String question, String dialogTitle) {
		// Ask user for Actor name until a valid name is entered.
		// For now, authorized refinements are other PiGraphs (.pi files) and
		// .idl prototypes
		Set<String> fileExtensions = new HashSet<String>();
		fileExtensions.add("pi");
		fileExtensions.add("idl");
		fileExtensions.add("h");
		IPath newFilePath = PiMMUtil.askFile(dialogTitle, question, null,
				fileExtensions);

		return newFilePath;
	}

	/**
	 * Set the refinement file of the actor and ask for the prototype in case
	 * the file is a C Header.
	 * 
	 * @param newFilePath
	 *            The {@link IPath} to the file. (Must not be null)
	 */
	protected void setActorRefinement(Actor actor, IPath newFilePath) {
		String dialogTitle = "Select a refinement file";
		Refinement refinement = actor.getRefinement();

		boolean validRefinement = false;
		do {
			// If the file is a .h header
			if (newFilePath.getFileExtension().equals("h")) {

				Set<FunctionPrototype> loopPrototypes;
				FunctionPrototype[] allProtoArray;
				IFile file;

				// We get it
				file = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(newFilePath);

				// Get all prototypes first (no filter)
				Set<FunctionPrototype> allPrototypes = getPrototypes(file,
						actor, PrototypeFilter.NONE);
				allProtoArray = allPrototypes
						.toArray(new FunctionPrototype[allPrototypes.size()]);

				loopPrototypes = getPrototypes(file, actor,
						PrototypeFilter.LOOP_ACTOR);
				validRefinement = (!loopPrototypes.isEmpty())
						|| (!allPrototypes.isEmpty());
				if (!validRefinement) {
					String message = "The .h file you selected does not contain any prototype."
							+ ".\nPlease select another valid file.";
					newFilePath = this.askRefinement(actor, message,
							dialogTitle);

					// If the cancel button of the dialog box was clicked
					// stop the setRefinement process.
					if (newFilePath == null) {
						return;
					}
				} else {
					// The file is a valid .h file.
					String title = "Loop Function Selection";
					String message = "Select a loop function for actor "
							+ actor.getName()
							+ "\n(* = any string, ? = any char):";
					FunctionPrototype[] loopProtoArray = loopPrototypes
							.toArray(new FunctionPrototype[loopPrototypes
									.size()]);
					FunctionPrototype loopProto = PiMMUtil
							.selectFunction(loopProtoArray, allProtoArray,
									title, message, true);

					Set<FunctionPrototype> initPrototypes = getPrototypes(file,
							actor, PrototypeFilter.INIT_ACTOR);
					Set<FunctionPrototype> allInitPrototypes = getPrototypes(
							file, actor, PrototypeFilter.INIT);

					FunctionPrototype initProto = null;
					if (!initPrototypes.isEmpty()
							|| !allInitPrototypes.isEmpty()) {
						title = "Init Function Selection";
						message = "Select an optionnal init function for actor "
								+ actor.getName()
								+ ", or click Cancel\n(* = any string, ? = any char):";
						FunctionPrototype[] initProtoArray = initPrototypes
								.toArray(new FunctionPrototype[initPrototypes
										.size()]);
						FunctionPrototype[] allInitProtoArray = allInitPrototypes
								.toArray(new FunctionPrototype[allInitPrototypes
										.size()]);
						initProto = PiMMUtil.selectFunction(initProtoArray,
								allInitProtoArray, title, message, false);

					}
					if (loopProto != null || initProto != null) {
						this.hasDoneChanges = true;
						HRefinement newRefinement = PiMMFactory.eINSTANCE
								.createHRefinement();
						newRefinement.setLoopPrototype(loopProto);
						newRefinement.setInitPrototype(initProto);
						newRefinement.setFilePath(newFilePath);
						actor.setRefinement(newRefinement);
					}
				}
			} else {
				// The file is either a .pi or a .IDL file.
				validRefinement = true;
				refinement.setFilePath(newFilePath);
				this.hasDoneChanges = true;
			}
		} while (!validRefinement);
	}

	private Set<FunctionPrototype> getPrototypes(IFile file, Actor actor,
			PrototypeFilter prototypeFilter) {
		Set<FunctionPrototype> result = new HashSet<FunctionPrototype>();

		if (file != null) {

			// Read the file (TODO Move this code in its own class)
			try {
				InputStream is = file.getContents();
				byte buffer[] = new byte[1000];

				int nbRead = 0;
				String fileContent = "";
				do {
					nbRead = is.read(buffer);
					fileContent = fileContent
							+ (new String(buffer)).substring(0, nbRead);
				} while (nbRead == 1000);

				// Filter unwanted content
				// Order of the filter is important !
				// Comments must be removed before pre-processing commands and
				// end of lines.

				// Filter comments between /* */
				Pattern pattern = Pattern.compile("(/\\*)(.*?)(\\*/)",
						Pattern.DOTALL);
				Matcher matcher = pattern.matcher(fileContent);
				fileContent = matcher.replaceAll("");

				// Filter comments between after //
				pattern = Pattern.compile("(//)(.*?\\n)", Pattern.DOTALL);
				matcher = pattern.matcher(fileContent);
				fileContent = matcher.replaceAll("");

				// Filter all pre-processing (
				pattern = Pattern.compile(
						"^\\s*#\\s*(([^\\\\]+?)((\\\\$[^\\\\]+?)*?$))",
						Pattern.MULTILINE | Pattern.DOTALL);
				matcher = pattern.matcher(fileContent);
				fileContent = matcher.replaceAll("");

				// Replace new lines and multiple spaces with a single space
				pattern = Pattern.compile("\\s+", Pattern.MULTILINE);
				matcher = pattern.matcher(fileContent);
				fileContent = matcher.replaceAll(" ");

				// Make sure there always is a space before and after each
				// group of * this will ease type identification during
				// prototype identification.
				// 1. remove all spaces around "*"
				pattern = Pattern.compile("\\s?\\*\\s?");
				matcher = pattern.matcher(fileContent);
				fileContent = matcher.replaceAll("*");
				// 2. add space around each groupe of *
				pattern = Pattern.compile(":?\\*+");
				matcher = pattern.matcher(fileContent);
				fileContent = matcher.replaceAll(" $0 ");

				// Identify and isolate prototypes in the remaining code
				// The remaining code is a single line containing only C code
				// (enum, struct, prototypes, inline functions, ..)
				pattern = Pattern.compile("[^;}](.*?\\(.*?\\))[;]");
				matcher = pattern.matcher(fileContent);
				List<String> prototypes = new ArrayList<String>();
				boolean containsPrototype;
				do {
					containsPrototype = matcher.find();
					if (containsPrototype) {
						prototypes.add(matcher.group(1));
					}
				} while (containsPrototype);

				// Create the FunctionPrototypes
				Set<FunctionPrototype> res = new HashSet<FunctionPrototype>();
				// Unique RegEx to separate the return type, the function name
				// and the list of parameters
				pattern = Pattern.compile("(.+?)\\s(\\S+?)\\s?\\((.*?)\\)");
				for (String prototypeString : prototypes) {
					System.out.println(prototypeString);
					FunctionPrototype funcProto = PiMMFactory.eINSTANCE
							.createFunctionPrototype();

					// Get the name
					matcher = pattern.matcher(prototypeString);
					if (matcher.matches()) { // apply the matcher
						funcProto.setName(matcher.group(2));
					}

					// Get the parameters (if any)
					// A new array list must be created because the list
					// returned by Arrays.asList cannot be modified (in
					// particular, no element can be removed from it).
					List<String> parameters = new ArrayList<String>(
							Arrays.asList(matcher.group(3).split("\\s?,\\s?")));
					// Remove empty match (is the function has no parameter)
					parameters.remove("");
					parameters.remove(" ");

					Pattern paramPattern = Pattern
							.compile("(IN|OUT)?([^\\*]+)(\\s\\**)?\\s(\\S+)");
					// Procces parameters one by one
					for (String param : parameters) {
						FunctionParameter fp = PiMMFactory.eINSTANCE
								.createFunctionParameter();
						matcher = paramPattern.matcher(param);
						boolean matched = matcher.matches();
						if (matched) { // Apply the matcher (if possible)
							// Get the parameter name
							fp.setName(matcher.group(4));
							// Get the parameter type
							fp.setType(matcher.group(2));
							// Check the direction (if any)
							if (matcher.group(1) != null) {
								if (matcher.group(1).equals("IN")) {
									fp.setDirection(Direction.IN);

								}
								if (matcher.group(1).equals("OUT")) {
									fp.setDirection(Direction.OUT);
								}
							}
							if (matcher.group(3) == null) {
								fp.setIsConfigurationParameter(true);
							}

							res.add(funcProto);
						}
					}
				}

				System.out.println(fileContent);

			} catch (CoreException | IOException e) {
				e.printStackTrace();
			}

			// With AST and CDT
			ICElement element = CoreModel.getDefault().create(file);
			ITranslationUnit tu = (ITranslationUnit) element;
			try {
				// Parse it
				IASTTranslationUnit ast = tu.getAST();
				ASTAndActorComparisonVisitor visitor = new ASTAndActorComparisonVisitor();
				ast.accept(visitor);
				// And extract from it the functions
				// compatible with the current actor
				switch (prototypeFilter) {
				case INIT_ACTOR:
					result = visitor.filterInitPrototypesFor(actor);
					break;
				case LOOP_ACTOR:
					result = visitor.filterLoopPrototypesFor(actor);
					break;
				case INIT:
					result = visitor.filterInitPrototypes();
					break;
				case NONE:
					result = visitor.getPrototypes();
					break;
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public boolean hasDoneChanges() {
		return this.hasDoneChanges;
	}

}
