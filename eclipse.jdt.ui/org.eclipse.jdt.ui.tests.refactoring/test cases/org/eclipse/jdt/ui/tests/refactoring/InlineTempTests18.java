/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.ui.tests.refactoring;

import junit.framework.Test;

public class InlineTempTests18 extends InlineTempTests {

	private static final Class clazz= InlineTempTests18.class;

	public InlineTempTests18(String name) {
		super(name);
	}

	public static Test suite() {
		return new Java18Setup(new NoSuperTestsSuite(clazz));
	}

	public static Test setUpTest(Test someTest) {
		return new Java18Setup(someTest);
	}

	@Override
	protected String getTestFileName(boolean canInline, boolean input) {
		String fileName= TEST_PATH_PREFIX + getRefactoringPath();
		fileName+= (canInline ? "canInline18/" : "cannotInline18/");
		return fileName + getSimpleTestFileName(canInline, input);
	}

	//--- tests for lambda expressions

	public void test0() throws Exception {
		helper1(6, 18, 6, 20);
	}

	public void test1() throws Exception {
		helper1(6, 18, 6, 20);
	}

	public void test2() throws Exception {
		helper1(5, 20, 5, 25);
	}
	
	public void test3() throws Exception {
		helper1(9, 29, 9, 36);
	}

	//--- tests for method references

	public void test1000() throws Exception {
		helper1(6, 18, 6, 20);
	}

	public void test1001() throws Exception {
		helper1(6, 18, 6, 20);
	}

}