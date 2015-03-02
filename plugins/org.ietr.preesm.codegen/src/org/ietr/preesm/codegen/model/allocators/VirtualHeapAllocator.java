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

package org.ietr.preesm.codegen.model.allocators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ietr.dftools.algorithm.model.sdf.SDFEdge;
import org.ietr.preesm.codegen.model.buffer.AbstractBufferContainer;
import org.ietr.preesm.codegen.model.buffer.Buffer;
import org.ietr.preesm.codegen.model.buffer.BufferAllocation;
import org.ietr.preesm.codegen.model.buffer.BufferToolBox;
import org.ietr.preesm.codegen.model.buffer.SubBuffer;
import org.ietr.preesm.codegen.model.buffer.SubBufferAllocation;
import org.ietr.preesm.codegen.model.expression.ConstantExpression;
import org.ietr.preesm.codegen.model.printer.CodeZoneId;
import org.ietr.preesm.codegen.model.printer.IAbstractPrinter;
import org.ietr.preesm.core.types.DataType;

public class VirtualHeapAllocator extends BufferAllocation implements
		IBufferAllocator {

	private static final int MIN_SIZE = 0;
	private Map<AbstractBufferContainer, HeapSectionAllocator> subHeap;
	private HeapSectionAllocator currentSection;
	protected int currentPos;
	private AbstractBufferContainer container;
	protected Map<SDFEdge, SubBufferAllocation> alloc;
	protected Map<BufferAllocation, Integer> allocToPos;
	protected int basePos;

	public VirtualHeapAllocator(AbstractBufferContainer container) {
		super(new Buffer("virtual_heap", MIN_SIZE, new DataType("char"),
				container));
		basePos = 0;
		this.container = container;
		currentPos = 0;
		subHeap = new HashMap<AbstractBufferContainer, HeapSectionAllocator>();
		alloc = new HashMap<SDFEdge, SubBufferAllocation>();
		allocToPos = new HashMap<BufferAllocation, Integer>();
	}

	@Override
	public HeapSectionAllocator openNewSection(
			AbstractBufferContainer codeSection) {
		currentSection = new HeapSectionAllocator(this, codeSection, currentPos);
		subHeap.put(codeSection, currentSection);
		allocToPos.put(currentSection, currentPos);
		return currentSection;

	}

	public void setSize(int size) {
		this.getBuffer().setSize(size);
	}

	@Override
	public SubBuffer addBuffer(SDFEdge edge, String name, DataType type) {
		int size = BufferToolBox.getBufferSize(edge);
		int trueSize = type.getSize() * size;
		SubBuffer newBuffer = new SubBuffer(name, size, new ConstantExpression(
				"", type, currentPos), new DataType(type), this.getBuffer(),
				edge, container);
		alloc.put(edge, new SubBufferAllocation(newBuffer));
		allocToPos.put(alloc.get(edge), currentPos);
		setSize(getSize() + trueSize);
		currentPos += trueSize;
		return newBuffer;
	}

	@Override
	public int getSize() {
		return this.getBuffer().getSize();
	}

	@Override
	public Buffer getBuffer(SDFEdge edge) {
		if (alloc.get(edge) != null) {
			return alloc.get(edge).getBuffer();
		}
		return null;
	}

	public int getCurrentPos() {
		return currentPos;
	}

	public int getBasePos() {
		return basePos;
	}

	public void removeBuffer(BufferAllocation buff) {
		for (SDFEdge key : alloc.keySet()) {
			if (alloc.get(key) == buff) {
				alloc.remove(key);
				setSize(getSize() - buff.getBuffer().getSize());
				refreshBufferPos(allocToPos.get(buff), buff.getSize());
				return;
			}
		}
	}

	public void refreshBufferPos(int oldBase, int diffSize) {
		for (BufferAllocation alloc : allocToPos.keySet()) {
			if (allocToPos.get(alloc) > oldBase) {
				if (alloc instanceof HeapSectionAllocator) {
					((HeapSectionAllocator) alloc).setBasePos(allocToPos
							.get(alloc) - diffSize);
					allocToPos.put(alloc, allocToPos.get(alloc) - diffSize);
				} else {
					((SubBuffer) alloc.getBuffer())
							.setIndex(new ConstantExpression(allocToPos
									.get(alloc) - diffSize));
					allocToPos.put(alloc, allocToPos.get(alloc) - diffSize);
				}
			}
		}
	}

	@Override
	public Buffer getBuffer(String name) {
		for (SDFEdge key : alloc.keySet()) {
			if (alloc.get(key).getBuffer().getName().equals(name)) {
				return alloc.get(key).getBuffer();
			}
		}
		return null;
	}

	@Override
	public List<BufferAllocation> getBufferAllocations() {
		return new ArrayList<BufferAllocation>(alloc.values());
	}

	@Override
	public boolean removeBufferAllocation(Buffer buff) {
		for (SDFEdge key : alloc.keySet()) {
			if (alloc.get(key).getBuffer() == buff) {
				alloc.remove(key);
				setSize(getSize() - buff.getSize());
				refreshBufferPos(allocToPos.get(buff), buff.getSize());
				return true;
			}
		}
		return false;
	}

	@Override
	public void accept(IAbstractPrinter printer, Object currentLocation) {
		currentLocation = printer.visit(this, CodeZoneId.body, currentLocation);
		List<IBufferAllocator> allocStack = new ArrayList<IBufferAllocator>();
		allocStack.add(this);
		while (allocStack.size() > 0) {
			if (allocStack.get(0) instanceof VirtualHeapAllocator) {
				for (BufferAllocation allocToPrint : allocStack.get(0)
						.getBufferAllocations()) {
					printer.visit((SubBufferAllocation) allocToPrint,
							CodeZoneId.body, currentLocation);
				}
				allocStack.addAll(((VirtualHeapAllocator) allocStack.get(0))
						.getChildAllocators());
			} else {
				allocStack.get(0).accept(printer, currentLocation);
			}
			allocStack.remove(0);
		}
	}

	@Override
	public List<IBufferAllocator> getChildAllocators() {
		return new ArrayList<IBufferAllocator>(subHeap.values());
	}

}
