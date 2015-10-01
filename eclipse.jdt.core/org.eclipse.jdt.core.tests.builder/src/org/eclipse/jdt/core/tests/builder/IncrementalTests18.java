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
package org.eclipse.jdt.core.tests.builder;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.osgi.framework.Bundle;

public class IncrementalTests18 extends BuilderTests {
	
	public IncrementalTests18(String name) {
		super(name);
	}

	public static Test suite() {
		return AbstractCompilerTest.buildMinimalComplianceTestSuite(IncrementalTests18.class, AbstractCompilerTest.F_1_8);
	}
	
	private void setupProjectForNullAnnotations() throws IOException, JavaModelException {
		// add the org.eclipse.jdt.annotation library (bin/ folder or jar) to the project:
		Bundle[] bundles = Platform.getBundles("org.eclipse.jdt.annotation","[2.0.0,3.0.0)");
		File bundleFile = FileLocator.getBundleFile(bundles[0]);
		String annotationsLib = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
		IJavaProject javaProject = env.getJavaProject("Project");
		IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
		int len = rawClasspath.length;
		System.arraycopy(rawClasspath, 0, rawClasspath = new IClasspathEntry[len+1], 0, len);
		rawClasspath[len] = JavaCore.newLibraryEntry(new Path(annotationsLib), null, null);
		javaProject.setRawClasspath(rawClasspath, null);

		javaProject.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423122, [1.8] Missing incremental build dependency from lambda expression to functional interface.
	public void test423122() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "I",
			"package p;	\n"+
			"public interface I { void foo(); }	\n"
		);
		env.addClass(root, "p", "X",
				"package p;	\n"+
				"public class X { I i = () -> {}; }	\n"
			);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "I",
				"package p;	\n"+
				"public interface I { }	\n"
			);
		incrementalBuild(projectPath);
		expectingProblemsFor(
			projectPath,
			"Problem : The target type of this expression must be a functional interface [ resource : </Project/src/p/X.java> range : <35,40> category : <40> severity : <2>]"
		);
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=423122, [1.8] Missing incremental build dependency from lambda expression to functional interface.
	public void test423122a() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "test1", "I",
				"package test1;\n" +
				"public interface I {\n" +
				"    int method(int a); // change argument type to Object\n" +
				"}\n"
		);
		env.addClass(root, "test1", "E",
				"package test1;\n" +
				"public class E {\n" +
				"    void take(I i) {\n" +
				"    }\n" +
				"}\n"
		);
		env.addClass(root, "test1", "Ref",
				"package test1;\n" +
				"public class Ref {\n" +
				"    void foo(E e) {\n" +
				"        e.take((x) -> x+2); // not recompiled when I#method changed\n" +
				"    }\n" +
				"}\n"
			);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "test1", "I",
				"package test1;\n" +
				"public interface I {\n" +
				"    int method(Object a); // change argument type to Object\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingProblemsFor(
			projectPath,
			"Problem : The operator + is undefined for the argument type(s) Object, int [ resource : </Project/src/test1/Ref.java> range : <76,79> category : <60> severity : <2>]"
		);
		env.addClass(root, "test1", "I",
				"package test1;\n" +
				"public interface I {\n" +
				"    int method(int a); // change argument type back to int\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427105, [1.8][builder] Differences between incremental and full builds in method contract verification in the presence of type annotations
	public void test427105() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "X",
				"import java.util.List;\n" +
				"public class X implements I {\n" +
				"	public void f(List x, List<I> ls) {                                      \n" +
				"	}\n" +
				"}\n"
		);
		env.addClass(root, "", "I",
				"import java.util.List;\n" +
				"public interface I {\n" +
				"	void f(@T List x, List<I> ls);\n" +
				"}\n"
		);
		env.addClass(root, "", "T",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"public @interface T {\n" +
				"}\n"
			);
		
		// force annotation encoding into bindings which is necessary to reproduce.
		env.getJavaProject("Project").setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

		fullBuild(projectPath);
		expectingProblemsFor(
				projectPath,
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/I.java> range : <55,59> category : <130> severity : <1>]\n" + 
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/X.java> range : <68,72> category : <130> severity : <1>]"
			);
		env.addClass(root, "", "X",
				"import java.util.List;\n" +
				"public class X implements I {\n" +
				"	public void f(List x, List<I> ls) {                                      \n" +
				"	}\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingProblemsFor(
				projectPath,
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/I.java> range : <55,59> category : <130> severity : <1>]\n" + 
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/X.java> range : <68,72> category : <130> severity : <1>]"
			);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=427105, [1.8][builder] Differences between incremental and full builds in method contract verification in the presence of type annotations
	public void test427105a() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "X",
				"import java.util.List;\n" +
				"public class X implements I {\n" +
				"	public void f(List x, List<I> ls) {                                      \n" +
				"	}\n" +
				"}\n"
		);
		env.addClass(root, "", "I",
				"import java.util.List;\n" +
				"public interface I {\n" +
				"	void f(@T List x, List<I> ls);\n" +
				"}\n"
		);
		env.addClass(root, "", "T",
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target(ElementType.TYPE_USE)\n" +
				"public @interface T {\n" +
				"}\n"
			);
		
		// force annotation encoding into bindings which is necessary to reproduce.
		env.getJavaProject("Project").setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

		fullBuild(projectPath);
		expectingProblemsFor(
				projectPath,
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/I.java> range : <55,59> category : <130> severity : <1>]\n" + 
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/X.java> range : <68,72> category : <130> severity : <1>]"
			);
		env.addClass(root, "", "X",
				"import java.util.List;\n" +
				"public class X implements I {\n" +
				"	public void f(@T List x, List<I> ls) {                                      \n" +
				"	}\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingProblemsFor(
				projectPath,
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/I.java> range : <55,59> category : <130> severity : <1>]\n" + 
				"Problem : List is a raw type. References to generic type List<E> should be parameterized [ resource : </Project/src/X.java> range : <71,75> category : <130> severity : <1>]"
			);
	}	
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=428071, [1.8][compiler] Bogus error about incompatible return type during override
	public void test428071() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "K1",
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"interface K1 {\n" +
				"	public Map<String,List> get();\n" +
				"}\n"
		);
		env.addClass(root, "", "K",
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"public class K implements K1 {\n" +
				"	public Map<String, List> get() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
		);
		env.getJavaProject("Project").setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		fullBuild(projectPath);
		expectingNoProblems();
		env.addClass(root, "", "K",
				"import java.util.List;\n" +
				"import java.util.Map;\n" +
				"public class K implements K1 {\n" +
				"	public Map<String, List> get() {\n" +
				"		return null;\n" +
				"	}\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=430425, [1.8][compiler] Type mismatch: cannot convert from StyleConverter<ParsedValue[],Insets> to StyleConverter<ParsedValue[],Insets>
	public void test430425() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		String jreDirectory = Util.getJREDirectory();
		String jfxJar = Util.toNativePath(jreDirectory + "/lib/ext/jfxrt.jar");
		File file = new File(jfxJar);
		if (file.exists())
			env.addExternalJars(projectPath, Util.concatWithClassLibs(jfxJar, false));
		else 
			return;

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "javafx.css", "StyleConverter",
				"package javafx.css;\n" +
				"import com.sun.javafx.css.converters.InsetsConverter;\n" +
				"import javafx.geometry.Insets;\n" +
				"public class StyleConverter<F, T> {\n" +
				"    public static StyleConverter<ParsedValue[], Insets> getInsetsConverter() {\n" +
				"        return InsetsConverter.getInstance();\n" +
				"    }\n" +
				"    void fred5555() {\n" +
				"    }\n" +
				"}\n"
		);
		env.addClass(root, "com.sun.javafx.css.converters", "InsetsConverter",
				"package com.sun.javafx.css.converters;\n" +
				"import com.sun.javafx.css.StyleConverterImpl;\n" +
				"import javafx.css.ParsedValue;\n" +
				"import javafx.css.StyleConverter;\n" +
				"import javafx.geometry.Insets;\n" +
				"public final class InsetsConverter extends StyleConverterImpl<ParsedValue[], Insets> {\n" +
				"    public static StyleConverter<ParsedValue[], Insets> getInstance() {\n" +
				"        return null;\n" +
				"    }\n" +
				"}\n"
		);
		env.addClass(root, "javafx.css", "ParsedValue",
				"package javafx.css;\n" +
				"public class ParsedValue<V, T> {\n" +
				"}\n"
		);
		env.getJavaProject("Project").setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
		fullBuild(projectPath);
		expectingNoProblems();
		env.addClass(root, "javafx.css", "StyleConverter",
				"package javafx.css;\n" +
				"import com.sun.javafx.css.converters.InsetsConverter;\n" +
				"import javafx.geometry.Insets;\n" +
				"public class StyleConverter<F, T> {\n" +
				"    public static StyleConverter<ParsedValue[], Insets> getInsetsConverter() {\n" +
				"        return InsetsConverter.getInstance();\n" +
				"    }\n" +
				"    void fred555() {\n" +
				"    }\n" +
				"}\n"
		);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}	

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=435544, [compiler][null] Enum constants not recognised as being NonNull (take2)
	public void test435544() throws JavaModelException, IOException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, "");

		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		setupProjectForNullAnnotations();
		env.addClass(root, "p", "Y",
				"package p;	\n" +
				 "public enum Y {\n" +
				 "	A,\n" +
				 "	B\n" +
				 "}\n" +
				 "\n"
		);

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(root, "p", "X",
				"package p;	\n" +
				"import org.eclipse.jdt.annotation.NonNull;\n" +
				"public class X {\n" +
				"	@NonNull\n" +
				"	public Y y = Y.A; // warning without fix\n" +
				"	void foo(@NonNull Y y) {}\n" +
				"   void bar() {\n" +
				"		foo(Y.A); // warning without fix\n" +
				"   }\n" +
				"}\n"
		);

		incrementalBuild(projectPath);
		expectingNoProblems();
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442452,  [compiler][regression] Bogus error: The interface Comparable cannot be implemented more than once with different arguments
	public void testBug442452() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "", "Entity", //$NON-NLS-1$ //$NON-NLS-2$
				"public class Entity implements IEntity<Entity> {\n" +
				"	public int compareTo(IBasicItem o) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n"); //$NON-NLS-1$

		env.addClass(projectPath, "", "IEntity", //$NON-NLS-1$ //$NON-NLS-2$
				"public interface IEntity<T extends IEntity<T>> extends IBasicItem {\n" +
				"}\n"); //$NON-NLS-1$
		
		env.addClass(projectPath, "", "IBasicItem", //$NON-NLS-1$ //$NON-NLS-2$
				"public interface IBasicItem extends Comparable<IBasicItem> {\n" +
				"}\n"); //$NON-NLS-1$
		
		env.addClass(projectPath, "", "IAdvancedItem", //$NON-NLS-1$ //$NON-NLS-2$
				"public interface IAdvancedItem extends Comparable<IBasicItem> {\n" +
				"}\n"); //$NON-NLS-1$

		fullBuild(projectPath);
		expectingNoProblems();

		env.addClass(projectPath, "", "Entity", //$NON-NLS-1$ //$NON-NLS-2$
				"public class Entity implements IEntity<Entity>, IAdvancedItem {\n" +
				"	public int compareTo(IBasicItem o) {\n" +
				"		return 0;\n" +
				"	}\n" +
				"}\n"); //$NON-NLS-1$

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442755,
	// [compiler] NPE at ProblemHandler.handle
	public void testBug442755() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin");
		env.addClass(projectPath, "", "Z",
			"public interface Z <X1 extends X, Y1 extends Y> {}\n");
		fullBuild(projectPath);
		expectingProblemsFor(
			projectPath,
			"Problem : X cannot be resolved to a type [ resource : </Project/Z.java>" +
			" range : <31,32> category : <40> severity : <2>]\n" +
			"Problem : Y cannot be resolved to a type [ resource : </Project/Z.java>" +
			" range : <45,46> category : <40> severity : <2>]");
		env.addClass(projectPath, "", "Unmarshaller", //$NON-NLS-1$ //$NON-NLS-2$
			"public abstract class Unmarshaller<CONTEXT extends Context, DESCRIPTOR extends Z> {\n" +
			"	public CONTEXT getContext() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n");
		incrementalBuild(projectPath);
		expectingProblemsFor(
			projectPath,
			"Problem : The project was not built since its build path is incomplete." +
			" Cannot find the class file for Y. Fix the build path then try building" +
			" this project [ resource : </Project> range : <-1,-1> category : <10> severity : <2>]\n" +
			"Problem : The type Y cannot be resolved. It is indirectly referenced from" +
			" required .class files [ resource : </Project/Unmarshaller.java> range : <0,1> category : <10> severity : <2>]");
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=442755,
	// [compiler] NPE at ProblemHandler.handle
	// Simplified test case.
	public void testBug442755a() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.8");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.setOutputFolder(projectPath, "bin");
		env.addClass(projectPath, "", "Z",
			"public class Z <Y2 extends Y> {}\n");
		fullBuild(projectPath);
		expectingProblemsFor(
				projectPath,
				"Problem : Y cannot be resolved to a type [ resource : " +
				"</Project/Z.java> range : <27,28> category : <40> severity : <2>]");
		env.addClass(projectPath, "", "X", //$NON-NLS-1$ //$NON-NLS-2$
				"public class X <Z> {}\n");
		incrementalBuild(projectPath);
		expectingProblemsFor(
			projectPath,
			"Problem : The project was not built since its build path is incomplete." +
			" Cannot find the class file for Y. Fix the build path then try building" +
			" this project [ resource : </Project> range : <-1,-1> category : <10> severity : <2>]\n" +
			"Problem : The type Y cannot be resolved. It is indirectly referenced from" +
			" required .class files [ resource : </Project/X.java> range : <0,1> category : <10> severity : <2>]");
	}
}
