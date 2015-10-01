/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.pause;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;

/**
 * Used to test performance in the IDE.  Processing @Pause
 * causes the processor to pause for a defined interval.
 */
public class PauseAnnotationProcessor extends BaseProcessor {

	private final AnnotationTypeDeclaration _annotationDecl;
	
	public PauseAnnotationProcessor(
			Set<AnnotationTypeDeclaration> decls, AnnotationProcessorEnvironment env) {
		super(env);
        assert decls.size() == 1;
        _annotationDecl = decls.iterator().next();
	}

	/* (non-Javadoc)
	 * @see com.sun.mirror.apt.AnnotationProcessor#process()
	 */
	public void process() {
		String phase = _env.getOptions().get("phase");
        Collection<Declaration> annotatedDecls = _env.getDeclarationsAnnotatedWith(_annotationDecl);
        for (Declaration decl : annotatedDecls) {
        	Pause a = decl.getAnnotation(Pause.class);
        	int pause = a.value();
        	System.out.println(phase + " pausing for " + pause + " to process " + decl.getSimpleName() + "...");
        	// busy sleep
        	long end = System.currentTimeMillis() + pause;
        	while (System.currentTimeMillis() < end)
        		for (int i = 0; i < 100000; ++i) {
        			/* pausing */
        		}
        	System.out.println(phase + " finished pausing");
        }
	}

}
