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

package org.ietr.workflow.converter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This class provides methods to transform an XML file via XSLT
 * 
 * @author Matthieu Wipliez
 * @author mpelcat
 * 
 */
public class XsltTransformer {

	private Transformer transformer;

	/**
	 * Creates a new {@link XsltTransform}
	 */
	public XsltTransformer() {
		super();
	}

	/**
	 * Sets an XSLT stylesheet contained in the file whose name is
	 * <code>fileName</code>.
	 * 
	 * @param fileName
	 *            The XSLT stylesheet file name.
	 * @throws TransformerConfigurationException
	 *             Thrown if there are errors when parsing the Source or it is
	 *             not possible to create a {@link Transformer} instance.
	 */
	public boolean setXSLFile(String xslFileLoc)
			throws TransformerConfigurationException {

		TransformerFactory factory = TransformerFactory.newInstance();

		StreamSource source = new StreamSource(xslFileLoc);

		try {
			transformer = factory.newTransformer(source);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (transformer == null) {
			System.out.println("XSL sheet not found or not valid: " + xslFileLoc);
		}

		return true;
	}

	/**
	 * Transforms the given input file and generates the output file
	 */
	public void transformFileToFile(String sourceFileLoc, String destFileLoc) {

		if (transformer != null) {

			try {
				transformer.transform(new StreamSource(sourceFileLoc),
						new StreamResult(new FileOutputStream(destFileLoc)));
			} catch (FileNotFoundException e1) {
				System.out.println("Problem finding files for XSL transfo ("
								+ sourceFileLoc + "," + destFileLoc + ")");
			} catch (TransformerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

}
