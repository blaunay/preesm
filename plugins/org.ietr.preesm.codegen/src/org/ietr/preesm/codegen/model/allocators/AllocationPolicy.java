package org.ietr.preesm.codegen.model.allocators;

import org.ietr.preesm.codegen.model.buffer.AbstractBufferContainer;

public class AllocationPolicy {

	public static AllocationPolicy instance = null;
	public static BufferAllocator allocatorType = BufferAllocator.Global;

	public static AllocationPolicy getInstance() {
		if (instance == null) {
			instance = new AllocationPolicy();
		}
		return instance;
	}

	public static void setAllocatorType(BufferAllocator alloc) {

		allocatorType = alloc;

	}

	protected AllocationPolicy() {

	}

	public IBufferAllocator getAllocator(AbstractBufferContainer container) {
		switch (allocatorType) {
		case Global:
			return new GlobalAllocator(container);
		case Local:
			return new LocalAllocator(container);
		case VirtualHeap:
			return new VirtualHeapAllocator(container);
		default:
			return new GlobalAllocator(container);
		}

	}

}
