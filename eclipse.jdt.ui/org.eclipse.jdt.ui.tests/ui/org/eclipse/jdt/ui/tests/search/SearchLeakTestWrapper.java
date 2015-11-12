/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
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

import org.eclipse.jdt.ui.leaktest.LeakTestSetup;


/**
 * This class is a wrapper for {@link SearchLeakTest}
 * in order to prevent the loading of the Search plug-in when the VM
 * verifies some JDT UI code.
 *
 * @since 3.4
 */
public class SearchLeakTestWrapper extends TestCase {

	private static final Class THIS= SearchLeakTestWrapper.class;

	SearchLeakTest fTest;
	private String fName;

	public SearchLeakTestWrapper(String name) {
		super(name);
		fName= name;
	}

	public static Test suite() {
		return setUpTest(new TestSuite(THIS));
	}

	public static Test setUpTest(Test test) {
		return new LeakTestSetup(new JUnitSourceSetup(test));
	}

	protected void setUp() throws Exception {
		fTest= new SearchLeakTest(fName);
		fTest.setUp();
	}

	protected void tearDown() throws Exception {
		fTest.tearDown();
		fTest= null;
	}

	public void testRemoveSearchQueries() throws Exception {
		fTest.testRemoveSearchQueries();
	}

	public void testRemoveAllQueries() throws Exception {
		fTest.testRemoveAllQueries();
	}

	public void testSearchResultEditorClose() throws Exception {
		fTest.testSearchResultEditorClose();
	}
}
