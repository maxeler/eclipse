/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *******************************************************************************/

package org.eclipse.jdt.internal.junit.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import org.eclipse.jdt.internal.junit.JUnitCorePlugin;


public class TestKind implements ITestKind {

	private final IConfigurationElement fElement;
	private ITestFinder fFinder;

	public TestKind(IConfigurationElement element) {
		fElement = element;
		fFinder= null;
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#createFinder()
	 */
	public ITestFinder getFinder() {
		if (fFinder == null) {
			try {
				fFinder= (ITestFinder) fElement.createExecutableExtension(FINDER_CLASS_NAME);
			} catch (CoreException e1) {
				JUnitCorePlugin.log(e1);
				fFinder= ITestFinder.NULL;
			}
		}
		return fFinder;
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#getDisplayName()
	 */
	public String getDisplayName() {
		return getAttribute(DISPLAY_NAME);
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#getFinderClassName()
	 */
	public String getFinderClassName() {
		return getAttribute(FINDER_CLASS_NAME);
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#getId()
	 */
	public String getId() {
		return getAttribute(ID);
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#getLoaderClassName()
	 */
	public String getLoaderClassName() {
		return getAttribute(LOADER_CLASS_NAME);
	}

	public String getLoaderPluginId() {
		return getAttribute(LOADER_PLUGIN_ID);
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#getPrecededKindId()
	 */
	public String getPrecededKindId() {
		String attribute= getAttribute(PRECEDES);
		return attribute == null ? "" : attribute; //$NON-NLS-1$
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#isNull()
	 */
	public boolean isNull() {
		return false;
	}

	protected String getAttribute(String attributeName) {
		return fElement.getAttribute(attributeName);
	}

	boolean precedes(ITestKind otherKind) {
		final String precededKindId = getPrecededKindId();
		String[] ids = precededKindId.split(","); //$NON-NLS-1$
		for (int i = 0; i < ids.length; i++) {
			if (ids[i].equals(otherKind.getId()))
				return true;
		}
		return false;
	}

	/*
	 * @see org.eclipse.jdt.internal.junit.launcher.ITestKind#getClasspathEntries()
	 */
	public JUnitRuntimeClasspathEntry[] getClasspathEntries() {
		IConfigurationElement[] children= fElement.getChildren(ITestKind.RUNTIME_CLASSPATH_ENTRY);
		JUnitRuntimeClasspathEntry[] returnThis= new JUnitRuntimeClasspathEntry[children.length];
		for (int i= 0; i < children.length; i++) {
			IConfigurationElement element= children[i];
			String pluginID= element.getAttribute(ITestKind.CLASSPATH_PLUGIN_ID);
			String pathToJar= element.getAttribute(ITestKind.CLASSPATH_PATH_TO_JAR);
			returnThis[i]= new JUnitRuntimeClasspathEntry(pluginID, pathToJar);
		}
		return returnThis;
	}

	/*
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getDisplayName() + " (id: " + getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
