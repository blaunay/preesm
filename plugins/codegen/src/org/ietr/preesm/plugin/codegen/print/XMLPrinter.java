/*********************************************************
Copyright or � or Copr. IETR/INSA: Matthieu Wipliez, Jonathan Piat,
Maxime Pelcat, Peng Cheng Mu, Jean-Fran�ois Nezan, Micka�l Raulet

[mwipliez,jpiat,mpelcat,pmu,jnezan,mraulet]@insa-rennes.fr

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

package org.ietr.preesm.plugin.codegen.print;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Comparator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.ietr.preesm.core.codegen.AbstractBufferContainer;
import org.ietr.preesm.core.codegen.AbstractCodeContainer;
import org.ietr.preesm.core.codegen.AbstractCodeElement;
import org.ietr.preesm.core.codegen.Buffer;
import org.ietr.preesm.core.codegen.BufferAllocation;
import org.ietr.preesm.core.codegen.CommunicationFunctionCall;
import org.ietr.preesm.core.codegen.CompoundCodeElement;
import org.ietr.preesm.core.codegen.FiniteForLoop;
import org.ietr.preesm.core.codegen.ForLoop;
import org.ietr.preesm.core.codegen.LinearCodeContainer;
import org.ietr.preesm.core.codegen.Receive;
import org.ietr.preesm.core.codegen.Semaphore;
import org.ietr.preesm.core.codegen.SemaphorePend;
import org.ietr.preesm.core.codegen.SemaphorePost;
import org.ietr.preesm.core.codegen.Send;
import org.ietr.preesm.core.codegen.SourceFile;
import org.ietr.preesm.core.codegen.SubBuffer;
import org.ietr.preesm.core.codegen.ThreadDeclaration;
import org.ietr.preesm.core.codegen.UserFunctionCall;
import org.ietr.preesm.core.codegen.VariableAllocation;
import org.ietr.preesm.core.codegen.printer.CodeZoneId;
import org.ietr.preesm.core.codegen.printer.IAbstractPrinter;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Visitor that generates XML code from source files. This code will be transformed
 * by xslt to the target language.
 * 
 * @author mpelcat
 */
public class XMLPrinter implements IAbstractPrinter {

	/**
	 * Current document
	 */
	private Document dom;
	
	public XMLPrinter() {
		super();

        try {
			DOMImplementation impl;
			impl = DOMImplementationRegistry.newInstance()
					.getDOMImplementation("Core 3.0 XML 3.0 LS");
			dom = impl.createDocument("http://org.ietr.preesm.sourceCode", "sourceCode",null);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void writeDom(IFile file) {

		try {
			// Gets the DOM implementation of document
			DOMImplementation impl = dom.getImplementation();
			DOMImplementationLS implLS = (DOMImplementationLS) impl;

			LSOutput output = implLS.createLSOutput();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			output.setByteStream(out);

			LSSerializer serializer = implLS.createLSSerializer();
			serializer.getDomConfig().setParameter("format-pretty-print", true);
			serializer.write(dom, output);
			
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true,
					false, new NullProgressMonitor());
			out.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Element getRoot(){
		return dom.getDocumentElement();
	}

	/**
	 * The core type determines which xsl transformation to apply
	 */
	public void setCoreType(String coreType){
		
		Element root = dom.getDocumentElement();
		Element eCoreType= dom.createElement("coreType");
		eCoreType.setTextContent(coreType);
		root.appendChild(eCoreType);
	}
	
	/**
	 * Compares two buffers by their alphabetical order
	 */
	public class AlphaOrderComparator implements
	Comparator<Buffer>{

		@Override
		public int compare(Buffer o1, Buffer o2) {
			return o1.getName().compareTo(o2.getName());
		}
		
	}

	// Buffers
	
	@Override
	public Object visit(AbstractBufferContainer domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element bufferContainer= dom.createElement("bufferContainer");
			((Element)currentLocation).appendChild(bufferContainer);
			currentLocation = bufferContainer;
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(Buffer domElt, CodeZoneId index, Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element buffer = dom.createElement("buffer");
			((Element)currentLocation).appendChild(buffer);

			buffer.setAttribute("name", domElt.getName());
			buffer.setAttribute("size", domElt.getSize().toString());
			buffer.setAttribute("type", domElt.getType().getTypeName());
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(SubBuffer domElt, CodeZoneId index,
			Object currentLocation) {
		if (index == CodeZoneId.body) {
			Element buffer = dom.createElement("subBuffer");
			((Element)currentLocation).appendChild(buffer);
			buffer.setAttribute("name", domElt.getName());
			buffer.setAttribute("parentBuffer", domElt.getParentBuffer().getName());
			buffer.setAttribute("index", domElt.getIndex().getName());
			buffer.setAttribute("size", domElt.getSize().toString());
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(BufferAllocation domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element bufferAllocation = dom.createElement("bufferAllocation");
			((Element)currentLocation).appendChild(bufferAllocation);
			
			bufferAllocation.setAttribute("name", domElt.getBuffer().getName());
			bufferAllocation.setAttribute("size", domElt.getBuffer().getSize().toString());
			bufferAllocation.setAttribute("type", domElt.getBuffer().getType().getTypeName());
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(VariableAllocation domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element variableAllocation = dom.createElement("variableAllocation");
			((Element)currentLocation).appendChild(variableAllocation);
			
			variableAllocation.setAttribute("name", domElt.getVariable().getName());
			variableAllocation.setAttribute("type", domElt.getVariable().getType().getTypeName());
		} 
		
		return currentLocation;
	}

	// Calls
	
	@Override
	public Object visit(AbstractCodeElement domElt, CodeZoneId index,
			Object currentLocation) {
		return currentLocation;
	}

	@Override
	public Object visit(UserFunctionCall domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element userFunctionCall = dom.createElement("userFunctionCall");
			((Element)currentLocation).appendChild(userFunctionCall);

			userFunctionCall.setAttribute("name", domElt.getName());
			currentLocation = userFunctionCall;
		} 
		
		return currentLocation;
	}

	// Code containers

	@Override
	public Object visit(AbstractCodeContainer domElt, CodeZoneId index,
			Object currentLocation) {
		return currentLocation;
	}
	
	@Override
	public Object visit(ForLoop domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element forLoop = dom.createElement("forLoop");
			((Element)currentLocation).appendChild(forLoop);
			
			currentLocation = forLoop;
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(LinearCodeContainer domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element linearCodeContainer = dom.createElement("linearCodeContainer");
			((Element)currentLocation).appendChild(linearCodeContainer);
			
			currentLocation = linearCodeContainer;
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(FiniteForLoop domElt, CodeZoneId index, Object currentLocation) {
		if (index == CodeZoneId.body) {
			Element forLoop = dom.createElement("finiteForLoop");
			forLoop.setAttribute("index", domElt.getIndex().toString());
			forLoop.setAttribute("domain", ((Integer) domElt.getNbIteration()).toString());
			((Element)currentLocation).appendChild(forLoop);
			currentLocation = forLoop;
		} 
		
		return currentLocation;
	}

	// Synchro

	@Override
	public Object visit(Semaphore domElt, CodeZoneId index,
			Object currentLocation) {
		
		if (index == CodeZoneId.body) {
			((Element)currentLocation).setAttribute("number", Integer.toString(domElt.getSemaphoreNumber()));
			((Element)currentLocation).setAttribute("type", domElt.getSemaphoreType().toString());
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(SemaphorePend domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element semaphorePend = dom.createElement("semaphorePend");
			((Element)currentLocation).appendChild(semaphorePend);
			currentLocation = semaphorePend;
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(SemaphorePost domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element semaphorePost = dom.createElement("semaphorePost");
			((Element)currentLocation).appendChild(semaphorePost);
			currentLocation = semaphorePost;
		} 
		
		return currentLocation;
	}

	// File and threads
	
	@Override
	public Object visit(SourceFile domElt, CodeZoneId index,
			Object currentLocation) {
		
		if (index == CodeZoneId.body) {
			Element sourceFileElt= dom.createElement("SourceFile");
			((Element)currentLocation).appendChild(sourceFileElt);
			currentLocation = sourceFileElt;
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(ThreadDeclaration domElt, CodeZoneId index,
			Object currentLocation) {
		
		if (index == CodeZoneId.body) {
			Element threadElt = dom.createElement("threadDeclaration");
			((Element)currentLocation).appendChild(threadElt);
			threadElt.setAttribute("name", domElt.getName());
			currentLocation = threadElt;
		}
		
		return currentLocation;
	}
	
	// Communication
	
	@Override
	public Object visit(CommunicationFunctionCall domElt, CodeZoneId index,
			Object currentLocation) {
		return currentLocation;
	}

	@Override
	public Object visit(Send domElt, CodeZoneId index, Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element send = dom.createElement("send");
			((Element)currentLocation).appendChild(send);
			
			send.setAttribute("medium", domElt.getMedium().getName());
			send.setAttribute("mediumDef", domElt.getMedium().getDefinition().getId());
			send.setAttribute("target", domElt.getTarget().getName());
			currentLocation = send;
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(Receive domElt, CodeZoneId index,
			Object currentLocation) {

		if (index == CodeZoneId.body) {
			Element receive = dom.createElement("receive");
			((Element)currentLocation).appendChild(receive);
			
			receive.setAttribute("medium", domElt.getMedium().getName());
			receive.setAttribute("mediumDef", domElt.getMedium().getDefinition().getId());
			receive.setAttribute("source", domElt.getSource().getName());
			currentLocation = receive;
		} 
		
		return currentLocation;
	}

	@Override
	public Object visit(CompoundCodeElement element, CodeZoneId index,
			Object currentLocation) {
		if (index == CodeZoneId.body) {
			Element compound = dom.createElement("CompoundCode");
			((Element)currentLocation).appendChild(compound);
			currentLocation = compound;
		} 
		
		return currentLocation;
	}

}
