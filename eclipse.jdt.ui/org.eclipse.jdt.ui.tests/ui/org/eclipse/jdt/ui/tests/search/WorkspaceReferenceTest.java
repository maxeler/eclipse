/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.search;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 */
public class WorkspaceReferenceTest extends TestCase {

	private static final Class THIS= WorkspaceReferenceTest.class;

	public static Test suite() {
		return setUpTest(new TestSuite(THIS));
	}

	public static Test setUpTest(Test test) {
		return new JUnitSourceSetup(test);
	}

	public WorkspaceReferenceTest(String name) {
		super(name);
	}

	public void testSimpleMethodRef() throws Exception {
		assertEquals(9, SearchTestHelper.countMethodRefs("junit.framework.Test", "countTestCases", new String[0]));
	}

	public void testFindOverridden() throws Exception {
		assertEquals(6, SearchTestHelper.countMethodRefs("junit.framework.TestCase", "countTestCases", new String[0]));
	}
}
