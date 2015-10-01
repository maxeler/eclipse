/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contribution for Bug 378024 - Ordering of comments between imports not preserved
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.osgi.service.prefs.BackingStoreException;

public class ImportRewriteTest extends AbstractJavaModelTests {

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;
	
	private static final Class THIS= ImportRewriteTest.class;

	protected IPackageFragmentRoot sourceFolder;

	public ImportRewriteTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test setUpTest(Test someTest) {
		TestSuite suite= new Suite("one test");
		suite.addTest(someTest);
		return suite;
	}

	public static Test suite() {
//		System.err.println("Warning, only part of the ImportRewriteTest are being executed!");
//		Suite suite = new Suite(ImportRewriteTest.class.getName());
//		suite.addTest(new ImportRewriteTest("testRemoveImports1"));
//		return suite;
		return allTests();
	}

	protected void setUp() throws Exception {
		super.setUp();

		IJavaProject proj= createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, String.valueOf(99));

		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(1));


		this.sourceFolder = getPackageFragmentRoot("P", "src");

		waitUntilIndexesReady();
	}

	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}


	public void testAddImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.net.Socket");
		imports.addImport("p.A");
		imports.addImport("com.something.Foo");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.net.Socket;\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import com.something.Foo;\n");
		buf.append("\n");
		buf.append("import p.A;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsNoEmptyLines() throws Exception {

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(0));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.util", "java.new", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.net.Socket;\n");
		buf.append("import p.A;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsMoreEmptyLines() throws Exception {

		this.sourceFolder.getJavaProject().setOption(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BETWEEN_IMPORT_GROUPS, String.valueOf(2));

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.util", "java.new", "p" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);

		imports.addImport("java.net.Socket");
		imports.addImport("p.A");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("import java.net.Socket;\n");
		buf.append("\n");
		buf.append("\n");
		buf.append("import p.A;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("java.x.Socket");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.x.Socket;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}


	public void testAddImports3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}
	
	public void testAddImports4() throws Exception {
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.util.Vector");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set; // comment\n");
		buf.append("import java.util.Vector ;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports5() throws Exception {
		getJavaProject("P").setOption(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_SEMICOLON, JavaCore.INSERT);
		
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map");
		imports.addImport("java.util.Set");
		imports.addImport("java.util.Map.Entry");
		imports.addImport("java.util.Collections");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.* ;\n");
		buf.append("import java.util.Map.Entry ;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=306568
	public void testAddImports6() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.util.Map.Entry");

		apply(imports);

		buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"import java.util.Map.Entry;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		assertEqualString(cu.getSource(), buf.toString());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=309022
	public void testAddImports7() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"import java.util.Map.Entry;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        PrintWriter pw;\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "java.util", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("java.io.PrintWriter");

		apply(imports);

		buf= new StringBuffer();
		buf.append(
				"package pack1;\n" + 
				"\n" + 
				"import java.io.*;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"import java.util.Map.Entry;\n" + 
				"\n" + 
				"public class C {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        HashMap h;\n" + 
				"\n" + 
				"        Map.Entry e= null;\n" + 
				"        Entry e2= null;\n" + 
				"\n" + 
				"        PrintWriter pw;\n" + 
				"        System.out.println(\"hello\");\n" + 
				"    }\n" + 
				"}");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsWithGroupsOfUnmatched1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "", "org", "#", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("org.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("java.util.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("import my.M;\n");
		buf.append("\n");
		buf.append("import org.x.Y;\n");
		buf.append("\n");
		buf.append("import static stat.X.CONST;\n");
		buf.append("\n");
		buf.append("import pack.P;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImportsWithGroupsOfUnmatched2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "org", "com", "pack", "#", "" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("com.x.Y");
		imports.addImport("pack.P");
		imports.addImport("my.M");
		imports.addImport("org.Vector");
		imports.addStaticImport("stat.X", "CONST", true);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import org.Vector;\n");
		buf.append("\n");
		buf.append("import com.x.Y;\n");
		buf.append("\n");
		buf.append("import pack.P;\n");
		buf.append("\n");
		buf.append("import static stat.X.CONST;\n");
		buf.append("\n");
		buf.append("import my.M;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List;\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Set");
		imports.removeImport("pack.List");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("import java.util.Map;\n");
		buf.append("\n");
		buf.append("import pack.List2;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("import java.util.Vector; // comment\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java", "com", "pack" };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.removeImport("java.util.Vector");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testRemoveImports3() throws Exception {
		IPackageFragment pack= this.sourceFolder.createPackageFragment("pack", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack;\n");
		buf.append("\n");
		buf.append("public class A {\n");
		buf.append("    public class Inner {\n");
		buf.append("    }\n");
		buf.append("}\n");
		pack.createCompilationUnit("A.java", buf.toString(), false, null);
		
		IPackageFragment test1= this.sourceFolder.createPackageFragment("test1", false, null);
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import pack.A;\n");
		buf.append("import pack.A.Inner;\n");
		buf.append("import pack.A.NotThere;\n");
		buf.append("import pack.B;\n");
		buf.append("import pack.B.Inner;\n");
		buf.append("import pack.B.NotThere;\n");
		buf.append("\n");
		buf.append("public class T {\n");
		buf.append("}\n");
		ICompilationUnit cuT= test1.createCompilationUnit("T.java", buf.toString(), false, null);
		
		ASTParser parser= ASTParser.newParser(JLS3_INTERNAL);
		parser.setSource(cuT);
		parser.setResolveBindings(true);
		CompilationUnit astRoot= (CompilationUnit) parser.createAST(null);
		
		ImportRewrite imports= newImportsRewrite(astRoot, new String[0], 99, 99, true);
		imports.setUseContextToFilterImplicitImports(true);
		
		imports.removeImport("pack.A.Inner");
		imports.removeImport("pack.A.NotThere");
		imports.removeImport("pack.B.Inner");
		imports.removeImport("pack.B.NotThere");
		
		apply(imports);
		
		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import pack.A;\n");
		buf.append("import pack.B;\n");
		buf.append("\n");
		buf.append("public class T {\n");
		buf.append("}\n");
		assertEqualString(cuT.getSource(), buf.toString());
	}

	public void testAddImports_bug23078() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import p.A.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { };

		ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
		imports.addImport("p.Inner");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import p.Inner;\n");
		buf.append("import p.A.*;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug25113() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.awt.Panel;\n");
		buf.append("\n");
		buf.append("import java.math.BigInteger;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java.awt", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.applet.Applet");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.awt.Panel;\n");
		buf.append("\n");
		buf.append("import java.applet.Applet;\n");
		buf.append("import java.math.BigInteger;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug42637() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.Exception;\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug121428() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.io.Exception;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	/**
	 * Test that the Inner class import comes in the right order (i.e. after the enclosing type's import) when re-organized
	 * 
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358() throws Exception {

		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack2.A;\n");
		buf.append("import pack2.A.Inner;\n");
		buf.append("import pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		// We need to actually make some state in the AST for the classes, to test that we can 
		// disambiguate between packages and inner classes (see the bug for details).
		IPackageFragment pack2= this.sourceFolder.createPackageFragment("pack2", false, null);
		ICompilationUnit aUnit= pack2.createCompilationUnit("A.java", "", false, null);
		ICompilationUnit bUnit= pack2.createCompilationUnit("B.java", "", false, null);
		bUnit.createType("class B {}", null, false, null);

		IType aType= aUnit.createType("class A {}", null, false, null);
		aType.createType("class Inner {}", null, false, null);
		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("pack2.A");
		imports.addImport("pack2.B");
		imports.addImport("pack2.A.Inner");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import pack2.A;\n");
		buf.append("import pack2.A.Inner;\n");
		buf.append("import pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	/**
	 * Test that a valid inner class import is not removed even when the container
	 * class is implicitly available. This tests the case where the classes are in 
	 * different compilation units.
	 * 
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194358"
	 */
	public void testBug194358a() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("package com.pack1;\n");
		buf.append("\n");
		buf.append("import com.pack1.A;\n");
		buf.append("import com.pack1.A.Inner;\n");
		buf.append("import com.pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("com.pack1", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);
		ICompilationUnit aUnit= pack1.createCompilationUnit("A.java", "", false, null);

		IPackageFragment pack2= this.sourceFolder.createPackageFragment("com.pack2", false, null);
		ICompilationUnit bUnit= pack2.createCompilationUnit("B.java", "", false, null);
		bUnit.createType("class B {}", null, false, null);
		IType aType= aUnit.createType("class A {}", null, false, null);
		aType.createType("class Inner {}", null, false, null);
		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(false);
		imports.addImport("com.pack1.A");
		imports.addImport("com.pack1.A.Inner");
		imports.addImport("com.pack2.B");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package com.pack1;\n");
		buf.append("\n");
		buf.append("import com.pack1.A.Inner;\n");
		buf.append("import com.pack2.B;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}
	/**
	 * Test that the Inner type imports are not removed while organizing even though the 
	 * containing class is implicitly available - for the case when both the classes are 
	 * in the same compilation unit
	 * 
	 * see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=235253"
	 */
	public void testBug235253() throws Exception {
		StringBuffer buf= new StringBuffer();
		buf.append("package bug;\n");
		buf.append("\n");
		buf.append("class Bug {\n");
		buf.append("public void addFile(File file) {}\n");
		buf.append("\tinterface Proto{};\n");
		buf.append("}\n");		
		buf.append("class Foo implements Proto{}");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("bug", false, null);
		ICompilationUnit cu= pack1.createCompilationUnit("Bug.java", buf.toString(), false, null);
		String[] order= new String[] { "bug" , "java" };
		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.setUseContextToFilterImplicitImports(true);
		imports.addImport("bug.Bug.Proto");
		imports.addImport("java.io.File"); 
		
		apply(imports);
		buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("\n");
		buf.append("import bug.Bug.Proto;\n");
		buf.append("\n");
		buf.append("import java.io.File;\n");
		buf.append("\n");
		buf.append("class Bug {\n");
		buf.append("public void addFile(File file) {}\n");
		buf.append("\tinterface Proto{};\n");
		buf.append("}\n");		
		buf.append("class Foo implements Proto{}");
		assertEqualString(cu.getSource(), buf.toString());
	}
		
	public void testAddStaticImports1() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addImport("java.lang.Math");
		imports.addStaticImport("java.lang.Math", "max", true);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.lang.Math.max;\n");
		buf.append("import static java.lang.Math.min;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddStaticImports2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("xx.MyConstants", "SIZE", true);
		imports.addStaticImport("xy.MyConstants", "*", true);
		imports.addImport("xy.MyConstants");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static xx.MyConstants.SIZE;\n");
		buf.append("import static xy.MyConstants.*;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("import xy.MyConstants;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddStaticImports3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 3, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addStaticImport("java.lang.Math", "max", true);
		imports.addStaticImport("java.lang.Math", "abs", true);

		imports.addStaticImport("java.io.File", "pathSeparator", true);
		imports.addStaticImport("java.io.File", "separator", true);

		imports.addImport("java.util.List");
		imports.addImport("java.util.Vector");
		imports.addImport("java.util.ArrayList");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.io.File.pathSeparator;\n");
		buf.append("import static java.io.File.separator;\n");
		buf.append("import static java.lang.Math.*;\n");
		buf.append("\n");
		buf.append("import java.lang.System;\n");
		buf.append("import java.util.ArrayList;\n");
		buf.append("import java.util.List;\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}


	private void createClassStub(String pack, String typeName, String typeKind) throws JavaModelException {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment(pack, false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package ").append(pack).append(";\n");
		buf.append("public ").append(typeKind).append(" ").append(typeName).append(" {\n");
		buf.append("}\n");
		String content= buf.toString();

		String name= typeName;
		int idx= typeName.indexOf('<');
		if (idx != -1) {
			name= typeName.substring(0, idx);
		}
		pack1.createCompilationUnit(name + ".java", content, false, null);
	}


	public void testImportStructureWithSignatures() throws Exception {
		createClassStub("java.io", "IOException", "class");
		createClassStub("java.net", "URL", "class");
		createClassStub("java.util", "List<E>", "interface");
		createClassStub("java.net", "SocketAddress", "class");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.net.*;\n");
		buf.append("import java.io.*;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        IOException s;\n");
		buf.append("        URL[][] t;\n");
		buf.append("        List<SocketAddress> x;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String content= buf.toString();
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("}\n");
		String content2= buf.toString();
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(JLS3_INTERNAL);
		ImportRewrite importsRewrite= newImportsRewrite(cu2, order, threshold, threshold, true);
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("IOException"), "IOException".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("URL"), "URL".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("List"), "List".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}
		apply(importsRewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.List;\n");
		buf.append("\n");
		buf.append("import java.io.IOException;\n");
		buf.append("\n");
		buf.append("import java.net.SocketAddress;\n");
		buf.append("import java.net.URL;\n");
		buf.append("\n");
		buf.append("public class B {\n");
		buf.append("}\n");

		assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
	}

	public void testImportStructureWithSignatures2() throws Exception {
		createClassStub("java.util", "Map<S, T>", "interface");
		createClassStub("java.util", "Set<S>", "interface");
		createClassStub("java.net", "SocketAddress", "class");
		createClassStub("java.net", "ServerSocket", "class");

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("import java.util.*;\n");
		buf.append("import java.net.*;\n");
		buf.append("import java.io.*;\n");
		buf.append("public class A {\n");
		buf.append("    public void foo() {\n");
		buf.append("        Map<?, ? extends Set<? super ServerSocket>> z;\n");
		buf.append("    }\n");
		buf.append("}\n");
		String content= buf.toString();
		ICompilationUnit cu1= pack1.createCompilationUnit("A.java", content, false, null);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class B {\n");
		buf.append("}\n");
		String content2= buf.toString();
		ICompilationUnit cu2= pack1.createCompilationUnit("B.java", content2, false, null);

		String[] order= new String[] { "java.util", "java.io", "java.net" };
		int threshold= 99;
		AST ast= AST.newAST(JLS3_INTERNAL);
		ImportRewrite importsRewrite= newImportsRewrite(cu2, order, threshold, threshold, true);
		{
			IJavaElement[] elements= cu1.codeSelect(content.indexOf("Map"), "Map".length());
			assertEquals(1, elements.length);
			String key= ((IType) elements[0]).getKey();
			String signature= new BindingKey(key).toSignature();

			importsRewrite.addImportFromSignature(signature, ast);
		}

		apply(importsRewrite);

		buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("\n");
		buf.append("import java.util.Map;\n");
		buf.append("import java.util.Set;\n");
		buf.append("\n");
		buf.append("import java.net.ServerSocket;\n");
		buf.append("\n");
		buf.append("public class B {\n");
		buf.append("}\n");

		assertEqualStringIgnoreDelim(cu2.getSource(), buf.toString());
	}


	public void testAddedRemovedImportsAPI() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public final static int CONST= 9;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addStaticImport("java.lang.Math", "min", true);
		imports.addImport("java.lang.Math");

		assertAddedAndRemoved(imports,
				new String[] { "java.lang.Math" }, new String[] {},
				new String[] { "java.lang.Math.min" }, new String[] {}
		);

		imports.addImport("java.lang.Math");
		imports.addStaticImport("java.lang.Math", "max", true);

		assertAddedAndRemoved(imports,
				new String[] { "java.lang.Math" }, new String[] {},
				new String[] { "java.lang.Math.min", "java.lang.Math.max" }, new String[] {}
		);

		imports.removeImport("java.lang.Math");
		imports.removeImport("java.util.Vector");
		imports.removeStaticImport("java.lang.Math.dup");

		assertAddedAndRemoved(imports,
				new String[] { }, new String[] { "java.util.Vector"},
				new String[] { "java.lang.Math.min", "java.lang.Math.max" }, new String[] {}
		);

		imports.addImport("java.util.Vector");
		imports.addStaticImport("pack1.C", "CONST", true);

		assertAddedAndRemoved(imports,
				new String[] { }, new String[] { },
				new String[] { "java.lang.Math.min", "java.lang.Math.max", "pack1.C.CONST" }, new String[] {}
		);

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import static java.lang.Math.max;\n");
		buf.append("import static java.lang.Math.min;\n");
		buf.append("import static pack1.C.CONST;\n");
		buf.append("\n");
		buf.append("import java.util.Vector;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("    public final static int CONST= 9;\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testPackageInfo() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("\npackage pack1;");

		ICompilationUnit cu= pack1.createCompilationUnit("package-info.java", buf.toString(), false, null);

		String[] order= new String[] { "#", "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("foo.Bar");

		apply(imports);

		buf= new StringBuffer();
		buf.append("\npackage pack1;\n");
		buf.append("import foo.Bar;\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testBug252379() throws CoreException, BackingStoreException,
			MalformedTreeException, BadLocationException {
		

		ICompilationUnit[] units = new ICompilationUnit[3];
		
		IPackageFragment pack1 = this.sourceFolder.createPackageFragment(
				"bug", false, null);

		StringBuffer buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("\n");
		buf.append("enum CaseType {\n");
		buf.append("\tone;\n");
		buf.append("\tstatic CaseType[] all(){return null;}\n");
		buf.append("}\n");
		
		units[0] = pack1.createCompilationUnit("CaseType.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("enum ShareLevel{all})\n");
		
		units[1] = pack1.createCompilationUnit("ShareLevel.java", buf.toString(), false, null);
		
		buf = new StringBuffer();
		buf.append("package bug;\n");
		buf.append("class Bug {\n");
		buf.append("public ShareLevel createControl() {\n");
		buf.append("for (CaseType cat : all())\n");
		buf.append("cat.hashCode();\n");
		buf.append("ShareLevel temp = all;\n");
		buf.append("return temp;\n");
		buf.append("};\n");
		buf.append("}\n");
		units[2] = pack1.createCompilationUnit("Bug.java", buf.toString(), false, null);

		ImportRewrite imports = newImportsRewrite(units[2], new String[] {}, 99, 99, false);
		imports.addStaticImport("bug.CaseType", "all", false);
		imports.addStaticImport("bug.ShareLevel", "all", true);

		apply(imports);

		buf = new StringBuffer();
		buf.append("package bug;\n\n");
		buf.append("import static bug.CaseType.all;\n");
		buf.append("import static bug.ShareLevel.all;\n\n");
		buf.append("class Bug {\n");
		buf.append("public ShareLevel createControl() {\n");
		buf.append("for (CaseType cat : all())\n");
		buf.append("cat.hashCode();\n");
		buf.append("ShareLevel temp = all;\n");
		buf.append("return temp;\n");
		buf.append("};\n");
		buf.append("}\n");
		assertEqualString(units[2].getSource(), buf.toString());
	}

	public void testAddImports_bug24804() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.String;\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.Exception;\n");
		buf.append("/** comment */\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug24804_2() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.AssertionError;//test\n");
		buf.append("\n");
		buf.append("/** comment2 */\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.Exception;\n");
		buf.append("import java.lang.AssertionError;//test\n");
		buf.append("\n");
		buf.append("/** comment2 */\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug24804_3() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.String;//test\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.Exception;\n");
		buf.append("//test\n");
		buf.append("/** comment */\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug24804_4() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.AssertionError;//test\n");
		buf.append("\n");
		buf.append("/** comment2 */\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System; /** comment3 */\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.Exception;\n");
		buf.append("//test\n");
		buf.append("/** comment2 */\n");
		buf.append("/** comment */\n");
		buf.append("/** comment3 */\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}

	public void testAddImports_bug24804_5() throws Exception {

		IPackageFragment pack1= this.sourceFolder.createPackageFragment("pack1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.lang.AssertionError; //test\n");
		buf.append("\n");
		buf.append("/** comment2 */\n");
		buf.append("\n");
		buf.append("/** comment */\n");
		buf.append("import java.lang.System;\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		String[] order= new String[] { "java" };

		ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
		imports.addImport("java.io.Exception");

		apply(imports);

		buf= new StringBuffer();
		buf.append("package pack1;\n");
		buf.append("\n");
		buf.append("import java.io.*;\n");
		buf.append("//test\n");
		buf.append("/** comment2 */\n");
		buf.append("/** comment */\n");
		buf.append("\n");
		buf.append("public class C {\n");
		buf.append("}\n");
		assertEqualString(cu.getSource(), buf.toString());
	}
	
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        // 2 imports are in 1 group but third is separated by a comment
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "import java.util.*; // test\n" +
                "import java.util.Map.Entry;\n" +
                "//comment 2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "import java.io.*;\n" + 
                "\n" + 
                "import java.util.*; // test\n" + 
                "import java.util.Map.Entry;\n" + 
                "//comment 2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" +
                "\n" +  
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_2() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        // all imports are in same group
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" + 
                "import java.util.*; // test\n" +
                "import java.util.Map.Entry; // test2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "// comment 1\n" + 
                "import java.io.*;\n" + 
                "\n" + 
                "import java.util.*; // test\n" +
                "import java.util.Map.Entry; // test2\n" +
                "import java.util.Map.SomethingElse;\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_3() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        // all imports are in same group
        // leading and trailing comments
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" + 
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.Entry; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "// comment 1\n" + 
                "/* lead 1*/ import java.io.*;\n" + 
                "\n" + 
                "import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.Entry; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" +  
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_3a() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" + 
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.Entry; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "// comment 1\n" +
				"/* lead 1*/ " +
				"import java.io.*;\n" + 
				"// test1\n" +
				"/* lead 2*/\n" +
				"// test2\n" +
				"/* lead 3*/ \n" +
				"// test3\n" +
				"// commen 3\n" +
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_4() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        // all imports are in same group
        // leading and trailing comments
        // two on demand imports in the group
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" + 
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "// comment 1\n" + 
                "/* lead 1*/ import java.io.*;\n" + 
                "\n" +  
                "import java.util.*; // test1\n" +
                "/* lead 2*/import java.util.Map.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_4a() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" + 
                "/* lead 1*/ import java.util.HashMap; // test1\n" +
                "/* lead 2*/import java.util.Map.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.io.PrintWriter");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
				"/* lead 1*/ " +
				"import java.io.*;\n" + 
				"// test1\n" +
				"/* lead 2*/\n" +
				"// test2\n" +
				"/* lead 3*/ \n" +
				"// test3\n" +
				"// commen 3\n" +
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_5() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        // all imports of same group are scattered around
        // leading and trailing comments
        // adding an on-demand import belonging to a group
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" + 
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "// comment 1\n" + 
                "/* lead 1*/ import java.util.*; // test1\n" +
                "import java.util.Map.*;\n" +
                "\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    public void testBug376930_5a() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        // all imports are in same group
        // leading and trailing comments
        // adding an on-demand import belonging to a group
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "// comment 1\n" + 
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "import java.util.Map.*;\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // added import should get folded into existing *, without touching comments
    public void testBug376930_5b() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
				"/* lead 2*//* lead 1*/ import java.util.*; // test1\n" +
				"// test2\n" +
				"/* lead 3*/ \n" +
				"// test3\n" +
				"// commen 3\n" +
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // remove imports, preserve all comments
    public void testBug376930_5c() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" + 
                "/* lead 1*/ import java.util.*; // test1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 3*/ import java.util.Map.SomethingElse; // test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.Map.*");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" + 
                "// comment 1\n" +
				"/* lead 1*/ " +
				"import java.util.Map.*; // test1\n" +
				"/* lead 2*/\n" +
				"// test2\n" +
				"/* lead 3*/ \n" +
				"// test3\n" +
				"// commen 3\n" +
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // added import should get folded along with existing import into *, without deleting comments
    public void testBug376930_5d() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "/* lead 1*/ import java.util.Map; // test1\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/* lead 2*/import java.io.PrintWriter.*; // test2\n" +
                "\n" +
                "/* lead 1*/ import java.util.*;\n" +
                " // test1\n" +
                "// commen 3\n" +
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "        PrintWriter pw;\n" + 
                "        System.out.println(\"hello\");\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376930
    // separating comment should not prevent folding into *-import
    public void testBug376930_5e() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" +
                "\n" +
                "import java.util.Map;\n" +
                "/* comment leading Map.Entry */\n" +
                "import java.util.Map.Entry;\n" +
                "\n" +
                "public class C {\n" +
                "    public static void main(String[] args) {\n" +
                "        HashMap h;\n" +
                "\n" +
                "        Map.Entry e= null;\n" +
                "        Entry e2= null;\n" +
                "\n" +
                "    }\n" +
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "javax", "org", "com" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "import java.util.*;\n" +
                "/* comment leading Map.Entry */\n" + 
                "import java.util.Map.Entry;\n" +
                "\n" + 
                "public class C {\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap h;\n" + 
                "\n" + 
                "        Map.Entry e= null;\n" + 
                "        Entry e2= null;\n" + 
                "\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    public void testBug378024() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "import java.awt.List;// test1\n" +
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * keep me with HashMap\n" +
                " */\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.awt", "java.io", "java.util" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "import java.awt.List;// test1\n\n" +
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "import java.io.Serializable;// test2\n\n" +
                "/*\n" +
                " * keep me with HashMap\n" +
                " */\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    public void testBug378024b() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "import java.awt.List;// test1\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "import java.awt.*;// test1\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "import java.io.*;// test2\n" +
                "\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.util.*;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports. 
    // comments in between stay where they are
    public void testBug378024c() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports. 
    // comments in between stay where they are
    public void testBug378024c_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // leading and trailing comments always move with imports, even if they get folded. 
    // comments in between stay where they are
    public void testBug378024c_2() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.*;// test1\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "import java.io.*;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "//lead 3\n" +
                "import java.util.*;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // not adding an import should preserve its comments and put them at the end.
    public void testBug378024d() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.*;// test1\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.*;// test3\n" +
                "// commen 3\n" + 
                "/*\n" +
                " * don't move me 4\n" +
                " */" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // adding a new import should not disturb comments and import should be added in its group
    public void testBug378024e() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "import java.io.*;\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // removing an import should preserve its comments at the end, and adding a new import should not disturb
    // existing comments
    public void testBug378024e_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "import java.io.PrintWriter;\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "/*\n" +
                " * don't move me 4\n" +
                " */" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // folding imports because of a newly added import should preserve comments
    public void testBug378024f() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "import java.io.*;\n" +
                "// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // folding imports because of a newly added import should preserve comments
    public void testBug378024f_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * keep me with Serializable 2\n" +
                " */\n" +
                "\n" +
                "// lead 3\n" +
                "import java.io.PrintWriter;// test3\n" +
                "/*\n" +
                " * keep me with PrintWriter\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me\n" +
                " */\n" +
                "\n" +
                "//lead 4\n" +
                "import java.util.HashMap;// test4\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 2, 2, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.util.HashMap");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * keep me with List\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "//lead 4\n" +
                "import java.util.HashMap;// test4\n" +
                "// commen 3\n" + 
                "/*\n" +
                " * keep me with Serializable\n" +
                " */\n" +
                "// lead 2\n" +
                "// lead 3\n" +
                "import java.io.*;// test3\n" +
                "/*\n" +
                " * keep me with PrintWriter\n" +
                " */\n" +
                "// test2\n" +
                "/*\n" +
                " * keep me with Serializable 2\n" +
                " */\n" +
                "/*\n" +
                " * don't move me\n" +
                " */\n" +
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Re-ordering imports and converting them to *
    public void testBug378024g() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.awt", "java.util", "java.io", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");

        apply(imports);

        StringBuffer buf2 = new StringBuffer();
        buf2.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.*;// test1\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.*;// test3\n" +
                "// commen 3\n" + 
                "\n" +
                "// lead 2\n" +
                "import java.io.*;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        List l = new List();\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf2.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are removed in case the restoring of imports is enabled
    // This will test changes in org.eclipse.jdt.internal.core.dom.rewrite.ImportRewriteAnalyzer.removeImport(String, boolean)
    public void testBug378024h() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.removeImport("java.awt.List");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "\n" +
                "// lead 1\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are removed in case the restoring of imports is enabled
    public void testBug378024h_1() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/* i am with List */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, true);
        imports.setUseContextToFilterImplicitImports(true);
        imports.removeImport("java.awt.List");
        imports.addImport("java.util.List");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "/* i am with List */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "import java.util.List;\n" +                
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are unfolded.
    public void testBug378024i() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.*;// test1\n" +
                "/* i am with List */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.*;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.*;// test3\n" +
                "// commen 3\n" + 
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap e= null;\n" + 
                "        PrintWriter p= null;\n" + 
                "        List l= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 99, 99, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.io.PrintWriter");
        imports.addImport("java.io.Serializable");
        imports.addImport("java.util.HashMap");
        imports.addImport("java.util.Map");
        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/* i am with List */\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "// lead 2\n" +
                "import java.io.PrintWriter;// test2\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "import java.io.Serializable;\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "// commen 3\n" + 
                "import java.util.Map;\n" +
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        HashMap e= null;\n" + 
                "        PrintWriter p= null;\n" + 
                "        List l= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }
    
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=378024
    // Preserve comments when imports are folded but a member type import is present
    public void testBug378024j() throws Exception {
        IPackageFragment pack1 = this.sourceFolder.createPackageFragment("pack1", false, null);
        StringBuffer buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.List;// test1\n" +
                "/* i am with List */\n" +
                "\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.HashMap;// test3\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "\n" +
                "/*keep me with Map.Entry*/\n" +
                "import java.util.Map.Entry;// member type import\n" +
                "/*keep me with Map.Entry 2*/\n" +
                "\n" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "\n" +
                "// lead 2\n" +
                "import java.io.Serializable;// test2\n" +
                "// commen 3\n" +
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        ICompilationUnit cu = pack1.createCompilationUnit("C.java", buf.toString(), false, null);

        String[] order = new String[] { "java", "java.util", "com", "pack" };

        ImportRewrite imports= newImportsRewrite(cu, order, 1, 1, false);
        imports.setUseContextToFilterImplicitImports(true);
        imports.addImport("java.awt.List");
        imports.addImport("java.util.HashMap");
        imports.addImport("java.util.Map.Entry");
        imports.addImport("java.io.Serializable");

        apply(imports);

        buf = new StringBuffer();
        buf.append(
                "package pack1;\n" + 
                "\n" +
                "// comment 1\n" +
                "/*\n" +
                " * don't move me 1\n" +
                " *\n" +
                " */\n" +
                "// lead 1\n" +
                "import java.awt.*;// test1\n" +
                "/* i am with List */\n" +
                "\n" +
                "//lead 3\n" +
                "import java.util.*;// test3\n" +
                "/*\n" +
                " * don't move me 3\n" +
                " */\n" +
                "/*keep me with Map.Entry*/\n" +
                "import java.util.Map.Entry;// member type import\n" +
                "/*keep me with Map.Entry 2*/\n" +
                "/*\n" +
                " * don't move me 2\n" +
                " */" +
                "/*\n" +
                " * don't move me 4\n" +
                " */\n" +
                "// lead 2\n" +
                "import java.io.*;// test2\n" +
                "// commen 3\n" +
                "\n" + 
                "public class C implements Serializable{\n" + 
                "    public static void main(String[] args) {\n" + 
                "        Map e= null;\n" + 
                "    }\n" + 
                "}");
        assertEqualString(cu.getSource(), buf.toString());
    }

	private void assertAddedAndRemoved(ImportRewrite imports, String[] expectedAdded, String[] expectedRemoved, String[] expectedAddedStatic, String[] expectedRemovedStatic) {
		assertEqualStringsIgnoreOrder(imports.getAddedImports(), expectedAdded);
		assertEqualStringsIgnoreOrder(imports.getAddedStaticImports(), expectedAddedStatic);
		assertEqualStringsIgnoreOrder(imports.getRemovedImports(), expectedRemoved);
		assertEqualStringsIgnoreOrder(imports.getRemovedStaticImports(), expectedRemovedStatic);
	}

	private void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}

	private void assertEqualStringsIgnoreOrder(String[] actual, String[] expecteds) {
		StringAsserts.assertEqualStringsIgnoreOrder(actual, expecteds);
	}

	private void assertEqualStringIgnoreDelim(String actual, String expected) throws IOException {
		StringAsserts.assertEqualStringIgnoreDelim(actual, expected);
	}

	private ImportRewrite newImportsRewrite(ICompilationUnit cu, String[] order, int normalThreshold, int staticThreshold, boolean restoreExistingImports) throws CoreException, BackingStoreException {
		ImportRewrite rewrite= ImportRewrite.create(cu, restoreExistingImports);
		rewrite.setImportOrder(order);
		rewrite.setOnDemandImportThreshold(normalThreshold);
		rewrite.setStaticOnDemandImportThreshold(staticThreshold);
		return rewrite;
	}

	protected ImportRewrite newImportsRewrite(CompilationUnit cu, String[] order, int normalThreshold, int staticThreshold, boolean restoreExistingImports) {
		ImportRewrite rewrite= ImportRewrite.create(cu, restoreExistingImports);
		rewrite.setImportOrder(order);
		rewrite.setOnDemandImportThreshold(normalThreshold);
		rewrite.setStaticOnDemandImportThreshold(staticThreshold);
		return rewrite;
	}

	private void apply(ImportRewrite rewrite) throws CoreException, MalformedTreeException, BadLocationException {
		TextEdit edit= rewrite.rewriteImports(null);

		// not the efficient way!
		ICompilationUnit compilationUnit= rewrite.getCompilationUnit();
		Document document= new Document(compilationUnit.getSource());
		edit.apply(document);
		compilationUnit.getBuffer().setContents(document.get());
	}

}
