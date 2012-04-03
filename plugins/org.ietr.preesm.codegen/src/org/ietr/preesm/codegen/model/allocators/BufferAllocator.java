package org.ietr.preesm.codegen.model.allocators;

public enum BufferAllocator {
	Global, Local, VirtualHeap;

	public static BufferAllocator fromString(String str) {
		if (str.equals("Global")) {
			return BufferAllocator.Global;
		} else if (str.equals("Local")) {
			return BufferAllocator.Local;
		} else if (str.equals("VirtualHeap")) {
			return BufferAllocator.VirtualHeap;
		}
		return null;
	}
}
