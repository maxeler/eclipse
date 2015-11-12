/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.junit.ui;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.buildpath.BuildPathSupport;
import org.eclipse.jdt.internal.junit.util.JUnitStubUtility;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.ClasspathFixProcessor;

public class JUnitClasspathFixProcessor extends ClasspathFixProcessor {

	private static class JUnitClasspathFixProposal extends ClasspathFixProposal {

		private final boolean fIsJunit4;
		private final int fRelevance;
		private final IJavaProject fProject;
		public JUnitClasspathFixProposal(IJavaProject project, boolean isJunit4, int relevance) {
			fProject= project;
			fIsJunit4= isJunit4;
			fRelevance= relevance;
		}

		@Override
		public String getAdditionalProposalInfo() {
			if (fIsJunit4) {
				return JUnitMessages.JUnitAddLibraryProposal_junit4_info;
			}
			return JUnitMessages.JUnitAddLibraryProposal_info;
		}

		@Override
		public Change createChange(IProgressMonitor monitor) throws CoreException {
			if (monitor == null) {
				monitor= new NullProgressMonitor();
			}
			monitor.beginTask(JUnitMessages.JUnitClasspathFixProcessor_progress_desc, 1);
			try {
				IClasspathEntry entry= null;
				if (fIsJunit4) {
					entry= BuildPathSupport.getJUnit4ClasspathEntry();
				} else {
					entry= BuildPathSupport.getJUnit3ClasspathEntry();
				}
				IClasspathEntry[] oldEntries= fProject.getRawClasspath();
				ArrayList<IClasspathEntry> newEntries= new ArrayList<IClasspathEntry>(oldEntries.length + 1);
				boolean added= false;
				for (int i= 0; i < oldEntries.length; i++) {
					IClasspathEntry curr= oldEntries[i];
					if (curr.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
						IPath path= curr.getPath();
						if (path.equals(entry.getPath())) {
							return new NullChange(); // already on build path
						} else if (path.matchingFirstSegments(entry.getPath()) > 0) {
							if (!added) {
								curr= entry; // replace
								added= true;
							} else {
								curr= null;
							}
						}
					} else if (curr.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
						IPath path= curr.getPath();
						if (path.segmentCount() > 0 && JUnitCorePlugin.JUNIT_HOME.equals(path.segment(0))) {
							if (!added) {
								curr= entry; // replace
								added= true;
							} else {
								curr= null;
							}
						}
					}
					if (curr != null) {
						newEntries.add(curr);
					}
				}
				if (!added) {
					newEntries.add(entry);
				}

				final IClasspathEntry[] newCPEntries= newEntries.toArray(new IClasspathEntry[newEntries.size()]);
				Change newClasspathChange= newClasspathChange(fProject, newCPEntries, fProject.getOutputLocation());
				if (newClasspathChange != null) {
					return newClasspathChange;
				}
			} finally {
				monitor.done();
			}
			return new NullChange();
		}

		@Override
		public String getDisplayString() {
			if (fIsJunit4) {
				return JUnitMessages.JUnitAddLibraryProposa_junit4_label;
			}
			return JUnitMessages.JUnitAddLibraryProposal_label;
		}

		@Override
		public Image getImage() {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
		}

		@Override
		public int getRelevance() {
			return fRelevance;
		}
	}

	private static final int JUNIT3= 1;
	private static final int JUNIT4= 2;


	@Override
	public ClasspathFixProposal[] getFixImportProposals(IJavaProject project, String missingType) throws CoreException {
		String s= missingType;
		int res= 0;
		if (s.startsWith("org.junit.")) { //$NON-NLS-1$
			res= JUNIT4;
		} else if (s.equals("TestCase") || s.equals("TestSuite") || s.startsWith("junit.")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			res= JUNIT3;
		} else if (s.equals("Test")) { //$NON-NLS-1$
			res= JUNIT3 | JUNIT4;
		} else if (s.equals("RunWith")) { //$NON-NLS-1$
			res= JUNIT4;
		}
		if (res != 0) {
			ArrayList<JUnitClasspathFixProposal> proposals= new ArrayList<JUnitClasspathFixProposal>();
			if ((res & JUNIT4) != 0 && JUnitStubUtility.is50OrHigher(project)) {
				proposals.add(new JUnitClasspathFixProposal(project, true, 15));
			}
			if ((res & JUNIT3) != 0) {
				proposals.add(new JUnitClasspathFixProposal(project, false, 15));
			}
			return proposals.toArray(new ClasspathFixProposal[proposals.size()]);
		}
		return null;
	}
}
