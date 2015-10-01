/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test for generic methods search using R_EQUIVALENT_MATCH rule.
 */
public class JavaSearchGenericMethodEquivalentTests extends JavaSearchGenericMethodTests {

	/**
	 * @param name
	 */
	public JavaSearchGenericMethodEquivalentTests(String name) {
		super(name, EQUIVALENT_RULE);
	}

	public static Test suite() {
		TestSuite suite = new Suite(JavaSearchGenericMethodEquivalentTests.class.getName());
		List tests = buildTestsList(JavaSearchGenericMethodEquivalentTests.class, 1, 0/* do not sort*/);
		for (int index=0, size=tests.size(); index<size; index++) {
			suite.addTest((Test)tests.get(index));
		}
		return suite;
	}

	/*
	 * Add line to result only if it is not an erasure match rule.
	 */
	void addResultLine(StringBuffer buffer, char[] line) {
		if (!CharOperation.match(RESULT_ERASURE_MATCH, line, true) &&
			!CharOperation.match(RESULT_POTENTIAL_MATCH, line, true)) {
			super.addResultLine(buffer, line);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.JavaSearchGenericTypeTests#removeFirstTypeArgument(char[])
	 */
	long removeFirstTypeArgument(char[] line) {
		return -1;
	}
}
