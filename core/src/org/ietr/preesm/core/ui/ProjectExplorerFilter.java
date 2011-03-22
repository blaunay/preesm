/**
 * 
 */
package org.ietr.preesm.core.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Filtering the .layout files that do not need to be displayed in the file
 * explorer
 * 
 * @author mpelcat
 */
public class ProjectExplorerFilter extends ViewerFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
	 * .Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		// TODO Auto-generated method stub

		if (element instanceof IFile) {

			IFile file = (IFile) element;
			String fileName = file.getName();
			if (fileName.endsWith(".layout")) {
				return false;
			}
		}

		return true;
	}

}
