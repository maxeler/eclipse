/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class VariablesInitializer extends ClasspathVariableInitializer {

	public static ITestInitializer initializer;

	public static interface ITestInitializer {
		public void initialize(String variable) throws JavaModelException;
	}

	public static void reset() {
		initializer = null;
		String[] varNames = JavaCore.getClasspathVariableNames();
		try {
			JavaCore.setClasspathVariables(varNames, new IPath[varNames.length], null);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	public static void setInitializer(ITestInitializer initializer) {
		VariablesInitializer.initializer = initializer;
	}

	public void initialize(String variable) {
		if (initializer == null) return;
		try {
			initializer.initialize(variable);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
}
