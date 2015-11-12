/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.junit.tests;

import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestElement.FailureTrace;
import org.eclipse.jdt.junit.model.ITestElement.ProgressState;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.jdt.testplugin.JavaProjectHelper;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.jdt.internal.junit.JUnitMessages;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;

public class TestRunListenerTest extends AbstractTestRunListenerTest {

	private String[] runSequenceTest(IType typeToLaunch) throws Exception {
		TestRunLog log= new TestRunLog();
		final TestRunListener testRunListener= new TestRunListeners.SequenceTest(log);
		JUnitCore.addTestRunListener(testRunListener);
		try {
			return launchJUnit(typeToLaunch, log);
		} finally {
			JUnitCore.removeTestRunListener(testRunListener);
		}
	}

	private String[] runTreeTest(IType typeToLaunch, int step) throws Exception {
		TestRunLog log= new TestRunLog();
		final TestRunListener testRunListener= new TestRunListeners.TreeTest(log, step);
		JUnitCore.addTestRunListener(testRunListener);
		try {
			return launchJUnit(typeToLaunch, TestKindRegistry.JUNIT3_TEST_KIND_ID, log);
		} finally {
			JUnitCore.removeTestRunListener(testRunListener);
		}
	}

	public void testOK() throws Exception {
		String source=
				"package pack;\n" +
				"import junit.framework.TestCase;\n" +
				"public class ATestCase extends TestCase {\n" +
				"    public void testSucceed() { }\n" +
				"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedSequence= new String[] {
			"sessionStarted-" + TestRunListeners.sessionAsString("ATestCase", ProgressState.RUNNING, Result.UNDEFINED, 0),
			"testCaseStarted-" + TestRunListeners.testCaseAsString("testSucceed", "pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 0),
			"testCaseFinished-" + TestRunListeners.testCaseAsString("testSucceed", "pack.ATestCase", ProgressState.COMPLETED, Result.OK, null, 0),
			"sessionFinished-" + TestRunListeners.sessionAsString("ATestCase", ProgressState.COMPLETED, Result.OK, 0)
		};
		String[] actual= runSequenceTest(aTestCase);
		assertEqualLog(expectedSequence, actual);
	}

	public void testFail() throws Exception {
		String source=
			"package pack;\n" +
			"import junit.framework.TestCase;\n" +
			"public class ATestCase extends TestCase {\n" +
			"    public void testFail() { fail(); }\n" +
			"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedSequence= new String[] {
			"sessionStarted-" + TestRunListeners.sessionAsString("ATestCase", ProgressState.RUNNING, Result.UNDEFINED, 0),
			"testCaseStarted-" + TestRunListeners.testCaseAsString("testFail", "pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 0),
			"testCaseFinished-" + TestRunListeners.testCaseAsString("testFail", "pack.ATestCase", ProgressState.COMPLETED, Result.FAILURE, new FailureTrace("junit.framework.AssertionFailedError", null, null), 0),
			"sessionFinished-" + TestRunListeners.sessionAsString("ATestCase", ProgressState.COMPLETED, Result.FAILURE, 0)
		};
		String[] actual= runSequenceTest(aTestCase);
		assertEqualLog(expectedSequence, actual);
	}

	public void testSimpleTest() throws Exception {
		String source=
			"package pack;\n" +
			"import junit.framework.*;\n" +
			"\n" +
			"public class ATestCase extends TestCase {\n" +
			"	protected int fValue1;\n" +
			"	protected int fValue2;\n" +
			"\n" +
			"	public ATestCase(String name) {\n" +
			"		super(name);\n" +
			"	}\n" +
			"	protected void setUp() {\n" +
			"		fValue1= 2;\n" +
			"		fValue2= 3;\n" +
			"	}\n" +
			"	public static Test suite() {\n" +
			"		// ensure ordering:\n" +
			"		TestSuite result= new TestSuite(\"ATestCase\");\n" +
			"		result.addTest(new ATestCase(\"testAdd\"));\n" +
			"		result.addTest(new ATestCase(\"testDivideByZero\"));\n" +
			"		result.addTest(new ATestCase(\"testEquals\"));\n" +
			"		return result;\n" +
			"	}\n" +
			"	public void testAdd() {\n" +
			"		double result= fValue1 + fValue2;\n" +
			"		// forced failure result == 5\n" +
			"		assertTrue(result == 6);\n" +
			"	}\n" +
			"	public void testDivideByZero() {\n" +
			"		int zero= 0;\n" +
			"		int result= 8/zero;\n" +
			"	}\n" +
			"	public void testEquals() {\n" +
			"		assertEquals(12, 12);\n" +
			"		assertEquals(12L, 12L);\n" +
			"		assertEquals(new Long(12), new Long(12));\n" +
			"\n" +
			"		assertEquals(\"Size\", String.valueOf(12), String.valueOf(13));\n" +
			"	}\n" +
			"	public static void main (String[] args) {\n" +
			"		junit.textui.TestRunner.run(suite());\n" +
			"	}\n" +
			"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedSequence= new String[] {
			"sessionStarted-" + TestRunListeners.sessionAsString("ATestCase", ProgressState.RUNNING, Result.UNDEFINED, 0),
			"testCaseStarted-" + TestRunListeners.testCaseAsString("testAdd", "pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 0),
			"testCaseFinished-" + TestRunListeners.testCaseAsString("testAdd", "pack.ATestCase", ProgressState.COMPLETED, Result.FAILURE, new FailureTrace("junit.framework.AssertionFailedError", null, null), 0),
			"testCaseStarted-" + TestRunListeners.testCaseAsString("testDivideByZero", "pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 0),
			"testCaseFinished-" + TestRunListeners.testCaseAsString("testDivideByZero", "pack.ATestCase", ProgressState.COMPLETED, Result.ERROR, new FailureTrace("java.lang.ArithmeticException", null, null), 0),
			"testCaseStarted-" + TestRunListeners.testCaseAsString("testEquals", "pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 0),
			"testCaseFinished-" + TestRunListeners.testCaseAsString("testEquals", "pack.ATestCase", ProgressState.COMPLETED, Result.FAILURE, new FailureTrace("junit.framework.ComparisonFailure", "12", "13"), 0),
			"sessionFinished-" + TestRunListeners.sessionAsString("ATestCase", ProgressState.COMPLETED, Result.ERROR, 0)
		};
		String[] actual= runSequenceTest(aTestCase);
		assertEqualLog(expectedSequence, actual);
	}


	public void testTreeOnSessionStarted() throws Exception {
		String source=
				"package pack;\n" +
				"import junit.framework.TestCase;\n" +
				"public class ATestCase extends TestCase {\n" +
				"    public void testSucceed() { }\n" +
				"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedTree= new String[] {
			TestRunListeners.sessionAsString("ATestCase", ProgressState.RUNNING, Result.UNDEFINED, 0),
			TestRunListeners.suiteAsString("pack.ATestCase", ProgressState.NOT_STARTED, Result.UNDEFINED, null, 1),
			TestRunListeners.testCaseAsString("testSucceed", "pack.ATestCase", ProgressState.NOT_STARTED, Result.UNDEFINED, null, 2),
		};
		String[] actual= runTreeTest(aTestCase, 1);
		assertEqualLog(expectedTree, actual);
	}

	public void testTreeOnSessionEnded() throws Exception {
		String source=
				"package pack;\n" +
				"import junit.framework.TestCase;\n" +
				"public class ATestCase extends TestCase {\n" +
				"    public void testFail() { fail(); }\n" +
				"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedTree= new String[] {
			TestRunListeners.sessionAsString("ATestCase", ProgressState.COMPLETED, Result.FAILURE, 0),
			TestRunListeners.suiteAsString("pack.ATestCase", ProgressState.COMPLETED, Result.FAILURE, null, 1),
			TestRunListeners.testCaseAsString("testFail", "pack.ATestCase", ProgressState.COMPLETED, Result.FAILURE, new FailureTrace("junit.framework.AssertionFailedError", null, null), 2),
		};
		String[] actual= runTreeTest(aTestCase, 4);
		assertEqualLog(expectedTree, actual);
	}

	public void testTreeOnSecondTestStarted() throws Exception {
		String source=
				"package pack;\n" +
				"import junit.framework.*;\n" +
				"public class ATestCase extends TestCase {\n" +
				"    public static Test suite() {\n" +
				"        // ensure ordering:\n" +
				"        TestSuite result= new TestSuite(\"pack.ATestCase\");\n" +
				"        result.addTest(new ATestCase(\"testSucceed\"));\n" +
				"        result.addTest(new ATestCase(\"testFail\"));\n" +
				"        return result;\n" +
				"    }\n" +
				"    public ATestCase(String name) {\n" +
				"        super(name);\n" +
				"    }\n" +
				"    public void testSucceed() { }\n" +
				"    public void testFail() { fail(); }\n" +
				"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedTree= new String[] {
			TestRunListeners.sessionAsString("ATestCase", ProgressState.RUNNING, Result.UNDEFINED, 0),
			TestRunListeners.suiteAsString("pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 1),
			TestRunListeners.testCaseAsString("testSucceed", "pack.ATestCase", ProgressState.COMPLETED, Result.OK, null, 2),
			TestRunListeners.testCaseAsString("testFail", "pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 2),
		};
		String[] actual= runTreeTest(aTestCase, 4);
		assertEqualLog(expectedTree, actual);
	}

	public void testTreeOnSecondTestStarted2() throws Exception {
		String source=
				"package pack;\n" +
				"import junit.framework.*;\n" +
				"public class ATestCase extends TestCase {\n" +
				"    public static Test suite() {\n" +
				"        // ensure ordering:\n" +
				"        TestSuite result= new TestSuite(\"pack.ATestCase\");\n" +
				"        result.addTest(new ATestCase(\"testFail\"));\n" +
				"        result.addTest(new ATestCase(\"testSucceed\"));\n" +
				"        return result;\n" +
				"    }\n" +
				"    public ATestCase(String name) {\n" +
				"        super(name);\n" +
				"    }\n" +
				"    public void testFail() { fail(); }\n" +
				"    public void testSucceed() { }\n" +
				"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedTree= new String[] {
			TestRunListeners.sessionAsString("ATestCase", ProgressState.RUNNING, Result.FAILURE, 0),
			TestRunListeners.suiteAsString("pack.ATestCase", ProgressState.RUNNING, Result.FAILURE, null, 1),
			TestRunListeners.testCaseAsString("testFail", "pack.ATestCase", ProgressState.COMPLETED, Result.FAILURE, new FailureTrace("junit.framework.AssertionFailedError", null, null), 2),
			TestRunListeners.testCaseAsString("testSucceed", "pack.ATestCase", ProgressState.RUNNING, Result.UNDEFINED, null, 2),
		};
		String[] actual= runTreeTest(aTestCase, 4);
		assertEqualLog(expectedTree, actual);
	}

	public void testTreeUnrootedEnded() throws Exception {
		// regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=153807
		String source=
				"package pack;\n" +
				"\n" +
				"import junit.framework.TestCase;\n" +
				"import junit.framework.TestResult;\n" +
				"import junit.framework.TestSuite;\n" +
				"\n" +
				"public class ATestCase extends TestCase {\n" +
				"    public static class RealTest extends TestCase {\n" +
				"        public RealTest(String name) {\n" +
				"            super(name);\n" +
				"        }\n" +
				"\n" +
				"        public void myTest1() throws Exception { }\n" +
				"\n" +
				"        public void myTest2() throws Exception {\n" +
				"            fail();\n" +
				"        }\n" +
				"    }\n" +
				"\n" +
				"    public void testAllTests() { }\n" +
				"\n" +
				"    public void run(TestResult result) {\n" +
				"        TestSuite suite = new TestSuite(\"MySuite\");\n" +
				"        suite.addTest(new RealTest(\"myTest1\"));\n" +
				"        suite.addTest(new RealTest(\"myTest2\"));\n" +
				"        suite.run(result);\n" +
				"    }\n" +
				"}";
		IType aTestCase= createType(source, "pack", "ATestCase.java");

		String[] expectedTree= new String[] {
			TestRunListeners.sessionAsString("ATestCase", ProgressState.COMPLETED, Result.FAILURE, 0),
			TestRunListeners.suiteAsString("pack.ATestCase", ProgressState.NOT_STARTED, Result.UNDEFINED, null, 1),
			TestRunListeners.testCaseAsString("testAllTests", "pack.ATestCase", ProgressState.NOT_STARTED, Result.UNDEFINED, null, 2),
			TestRunListeners.suiteAsString(JUnitMessages.TestRunSession_unrootedTests, ProgressState.COMPLETED, Result.FAILURE, null, 1),
			TestRunListeners.testCaseAsString("myTest1", "pack.ATestCase.RealTest", ProgressState.COMPLETED, Result.OK, null, 2),
			TestRunListeners.testCaseAsString("myTest2", "pack.ATestCase.RealTest", ProgressState.COMPLETED, Result.FAILURE, new FailureTrace("junit.framework.AssertionFailedError", null, null), 2),
		};
		String[] actual= runTreeTest(aTestCase, 6);
		assertEqualLog(expectedTree, actual);
	}
	
	public void testTreeJUnit4TestAdapter() throws Exception {
		// regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=397747
		IClasspathEntry cpe= JavaCore.newContainerEntry(JUnitCore.JUNIT4_CONTAINER_PATH);
		JavaProjectHelper.clear(fProject, new IClasspathEntry[] { cpe });
		JavaProjectHelper.addRTJar15(fProject);
		
		String source=
				"package test;\n" + 
				"\n" + 
				"import junit.framework.JUnit4TestAdapter;\n" + 
				"import junit.framework.TestCase;\n" + 
				"import junit.framework.TestSuite;\n" + 
				"\n" + 
				"import org.junit.Test;\n" + 
				"import org.junit.runner.RunWith;\n" + 
				"import org.junit.runners.Suite;\n" + 
				"import org.junit.runners.Suite.SuiteClasses;\n" + 
				"\n" + 
				"public class MyTestSuite {\n" + 
				"	public static junit.framework.Test suite() {\n" + 
				"		TestSuite suite = new TestSuite();\n" + 
				"		suite.addTest(new JUnit4TestAdapter(JUnit4TestSuite.class));\n" + 
				"		suite.addTestSuite(JUnit3TestCase.class);\n" + 
				"		return suite;\n" + 
				"	}\n" + 
				"	\n" + 
				"	@RunWith(Suite.class)\n" + 
				"	@SuiteClasses({JUnit4TestCase.class})\n" + 
				"	static class JUnit4TestSuite {}\n" + 
				"	\n" + 
				"	public static class JUnit4TestCase {\n" + 
				"		@Test public void testA() {}\n" + 
				"		@Test public void testB() {}\n" + 
				"	}\n" + 
				"	\n" + 
				"	public static class JUnit3TestCase extends TestCase {\n" + 
				"		public void testC() {}\n" + 
				"		public void testD() {}\n" + 
				"		public void testE() {}\n" + 
				"	}\n" + 
				"}\n";
		IType aTestCase= createType(source, "test", "MyTestSuite.java");
		
		String[] expectedTree= new String[] {
				TestRunListeners.sessionAsString("MyTestSuite", ProgressState.COMPLETED, Result.OK, 0),
				TestRunListeners.suiteAsString("junit.framework.TestSuite", ProgressState.COMPLETED, Result.OK, null, 1),
				
				TestRunListeners.suiteAsString("test.MyTestSuite.JUnit4TestSuite", ProgressState.COMPLETED, Result.OK, null, 2),
				TestRunListeners.suiteAsString("test.MyTestSuite.JUnit4TestCase", ProgressState.COMPLETED, Result.OK, null, 3),
				TestRunListeners.testCaseAsString("testA", "test.MyTestSuite.JUnit4TestCase", ProgressState.COMPLETED, Result.OK, null, 4),
				TestRunListeners.testCaseAsString("testB", "test.MyTestSuite.JUnit4TestCase", ProgressState.COMPLETED, Result.OK, null, 4),
				
				TestRunListeners.suiteAsString("test.MyTestSuite.JUnit3TestCase", ProgressState.COMPLETED, Result.OK, null, 2),
				TestRunListeners.testCaseAsString("testC", "test.MyTestSuite.JUnit3TestCase", ProgressState.COMPLETED, Result.OK, null, 3),
				TestRunListeners.testCaseAsString("testD", "test.MyTestSuite.JUnit3TestCase", ProgressState.COMPLETED, Result.OK, null, 3),
				TestRunListeners.testCaseAsString("testE", "test.MyTestSuite.JUnit3TestCase", ProgressState.COMPLETED, Result.OK, null, 3),
		};
		String[] actual= runTreeTest(aTestCase, 12);
		assertEqualLog(expectedTree, actual);
	}
}
