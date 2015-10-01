/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/


package org.eclipse.jdt.apt.tests.annotations.nestedhelloworld;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;


public class NestedHelloWorldAnnotationProcessor extends
		BaseProcessor {

	public NestedHelloWorldAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		super( env );
	}

	// Code is annotated with HelloWorldAnnotation, so it will cause another round of processing
	public String getCode() {
		return "package " + PACKAGENAME + ";" + "\n" + 
		"@" + HelloWorldAnnotation.class.getName() + "\n" + 
		"public class " + TYPENAME + "\n" +
		"{  }";
	}
		
	private final static String PACKAGENAME = "nested.hello.world.generatedclass.pkg"; //$NON-NLS-1$
	private final static String TYPENAME = "NestedHelloWorldAnnotationGeneratedClass"; //$NON-NLS-1$
	
	@SuppressWarnings("unused")
	public void process()
	{
		Filer f = _env.getFiler();
		AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration) _env.getTypeDeclaration(NestedHelloWorldAnnotation.class.getName());
		Collection<Declaration> annotatedDecls = _env.getDeclarationsAnnotatedWith(annoDecl);
		try {
			for (Declaration annotatedDecl : annotatedDecls) {
				String typeName = TYPENAME;
				PrintWriter writer = f.createSourceFile(
						PACKAGENAME + "." + typeName);
				writer.print(getCode());
				writer.close();
			}
			reportSuccess(this.getClass());
		}
		catch (NullPointerException npe) {
			reportError(this.getClass(), "Could not read annotation in order to generate text file");
		}
		catch (IOException ioe) {
			reportError(this.getClass(), "Could not generate text file due to IOException");
		}
	}
}
