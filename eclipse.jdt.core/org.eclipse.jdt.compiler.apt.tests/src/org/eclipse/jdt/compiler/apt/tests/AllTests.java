/*******************************************************************************
 * Copyright (c) 2006, 2013 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - Java 8 support
 *    
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Helper class to run all the compiler tool tests
 */
public class AllTests extends TestCase {
	// run all tests
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(FileManagerTests.class);
		suite.addTestSuite(BatchDispatchTests.class);
		suite.addTestSuite(ModelTests.class);
		suite.addTestSuite(MessagerTests.class);
		suite.addTestSuite(FilerTests.class);
		suite.addTestSuite(ModelUtilTests.class);
		suite.addTestSuite(NegativeTests.class);
		suite.addTestSuite(Java8ElementsTests.class);
		return suite;
	}
}
