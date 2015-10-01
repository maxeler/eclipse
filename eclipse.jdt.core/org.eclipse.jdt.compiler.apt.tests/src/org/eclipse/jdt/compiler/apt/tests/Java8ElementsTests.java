/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class Java8ElementsTests extends TestCase {
	
	private static final String JAVA8_ANNOTATION_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.Java8ElementProcessor";

	public static Test suite() {
		return new TestSuite(Java8ElementsTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	public void testSE8Specifics() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testSE8Specifics");
	}
	public void _testSE8SpecificsWithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testSE8Specifics");
	}
	public void testLambdaSpecifics() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testLambdaSpecifics");
	}
	public void testLambdaSpecificsWithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testLambdaSpecifics");
	}
	public void testTypeAnnotations() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations");
	}
	public void _testTypeAnnotationsWithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations");
	}
	public void testTypeAnnotations1() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations1");
	}
	public void testTypeAnnotations1WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations1");
	}
	public void testTypeAnnotations2() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations2");
	}
	public void _testTypeAnnotations2WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations2");
	}
	public void testTypeAnnotations3() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations3");
	}
	public void _testTypeAnnotations3WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations3");
	}
	public void testTypeAnnotations4() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations4");
	}
	public void _testTypeAnnotations4WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations4");
	}
	public void testTypeAnnotations5() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations5");
	}
	public void _testTypeAnnotations5WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations5");
	}
	public void testTypeAnnotations6() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations6");
	}
	public void _testTypeAnnotations6WithJavac() throws Exception {   // Disabled for now. Javac 8b108 drops annotations arrays preceding varargs.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations6");
	}
	public void testTypeAnnotations7() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations7");
	}
	public void _testTypeAnnotations7WithJavac() throws Exception {  // Disabled for now. Javac 8b108 misattributes annotations on type parameters 
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations7");
	}
	public void testTypeAnnotations8() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations8");
	}
	public void _testTypeAnnotations8WithJavac() throws Exception {     // Disabled for now. Javac 8b108 misattributes annotations on type parameters
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations8");
	}
	public void testTypeAnnotations9() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations9");
	}
	public void _testTypeAnnotations9WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations9");
	}
	public void testTypeAnnotations10() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations10");
	}
	public void testTypeAnnotations10WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations10");
	}
	public void testTypeAnnotations11() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations11");
	}
	public void _testTypeAnnotations11WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations11");
	}
	public void testTypeAnnotations12() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations12");
	}
	public void testTypeAnnotations12WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations12");
	}
	public void testTypeAnnotations13() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations13");
	}
	public void testTypeAnnotations13WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations13");
	}
	public void testTypeAnnotations14() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations14");
	}
	public void _testTypeAnnotations14WithJavac() throws Exception { // Disabled for now. Javac returns null as receiver type where it should be type 'None'
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations14");
	}
	public void testTypeAnnotations15() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations15", "Z1.java");
	}
	public void testTypeAnnotations15WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations15", "Z1.java");
	}
	public void testTypeAnnotations16() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations16", "Z2.java");
	}
	public void testTypeAnnotations16WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations16", "Z2.java");
	}
	public void testRepeatedAnnotations17() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations17", "JEP120.java");
	}
	public void testRepeatedAnnotations17WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations17", "JEP120.java");
	}
	public void testRepeatedAnnotations18() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations18", "JEP120_1.java");
	}
	public void testRepeatedAnnotations18WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations18", "JEP120_1.java");
	}
	public void testRepeatedAnnotations19() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations19", "JEP120_2.java");
	}
	public void testRepeatedAnnotations19WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations19", "JEP120_2.java");
	}
	public void testRepeatedAnnotations20() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations20", "JEP120_3.java");
	}
	public void testRepeatedAnnotations20WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations20", "JEP120_3.java");
	}
	
	public void testRepeatedAnnotations21() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations21", "JEP120_4.java");
	}
	public void testRepeatedAnnotations21WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations21", "JEP120_4.java");
	}
	
	public void testRepeatedAnnotations22() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations22", "JEP120_5.java");
	}
	
	public void _testRepeatedAnnotations22WithJavac() throws Exception { // Disabled for now, javac 8b108 does not seem to expose any annotations on a type mirror when there are repeated annotations.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations22", "JEP120_5.java");
	}
	
	public void testTypeAnnotations23() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations23");
	}

	public void testTypeAnnotations23WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations23");
	}

	public void testRepeatedAnnotations24() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations24", "JEP120_6.java");
	}

	public void testRepeatedAnnotations24WithJavac() throws Exception { // Disabled for now, javac 8b108 does not seem to expose any annotations on a type mirror when there are repeated annotations.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations24", "JEP120_6.java");
	}
	
	public void testRepeatedAnnotations25() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations25", "JEP120_7.java");
	}

	public void testRepeatedAnnotations25WithJavac() throws Exception { // Disabled for now, javac 8b108 does not seem to expose any annotations on a type mirror when there are repeated annotations.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testRepeatedAnnotations25", "JEP120_7.java");
	}

	public void testTypeAnnotations26() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations26");
	}

	public void testTypeAnnotations26WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations26");
	}
	
	public void _testTypeAnnotations27() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations27");
	}
	
	public void _testTypeAnnotations27WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testTypeAnnotations27");
	}
	public void testPackageAnnotations() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testPackageAnnotations", null, "filer8");
	}
	public void testPackageAnnotationsWithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, JAVA8_ANNOTATION_PROC, "testPackageAnnotations", null, "filer8");
	}
	
	private void internalTest(JavaCompiler compiler, String processor, String testMethod) throws IOException {
		internalTest(compiler, processor, testMethod, null);
	}
	private void internalTest(JavaCompiler compiler, String processor, String testMethod, String testClass) throws IOException {
		internalTest(compiler, processor, testMethod, testClass, "model8");
	}
	private void internalTest(JavaCompiler compiler, String processor, String testMethod, String testClass, String resourceArea) throws IOException {
		if (!canRunJava8()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("targets/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("targets/" + resourceArea + "/" + testClass, targetFolder);
		}
		

		List<String> options = new ArrayList<String>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		// Javac 1.8 doesn't (yet?) support the -1.8 option
		if (compiler instanceof EclipseCompiler) {
			options.add("-1.8");
		}
		BatchTestUtils.compileTree(compiler, options, targetFolder, true);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	public boolean canRunJava8() {
		try {
			SourceVersion.valueOf("RELEASE_8");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
