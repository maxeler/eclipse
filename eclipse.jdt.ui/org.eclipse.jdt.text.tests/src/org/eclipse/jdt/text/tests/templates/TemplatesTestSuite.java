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
package org.eclipse.jdt.text.tests.templates;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Template test suite.
 *
 * @since 3.4
 */
public class TemplatesTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(TemplatesTestSuite.class.getName());
		//$JUnit-BEGIN$
		suite.addTest(TemplateContributionTest.suite());
		//$JUnit-END$
		return suite;
	}
}
