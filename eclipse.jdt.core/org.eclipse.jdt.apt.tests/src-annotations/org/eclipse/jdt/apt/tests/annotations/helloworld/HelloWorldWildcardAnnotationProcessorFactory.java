/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.helloworld;

import java.util.Collection;
import java.util.Collections;

/**
 * Processor factory that claims annotations with a wildcard 
 * ("org.eclipse.jdt.apt.tests.annotations.helloworld.*")
 */
public class HelloWorldWildcardAnnotationProcessorFactory extends
		HelloWorldAnnotationProcessorFactory {
	
	public static volatile boolean CLAIM_ALL_ANNOTATIONS;

	
	public Collection<String> supportedAnnotationTypes() {
		
		// We need to swap behavior because always claiming "*" will cause
		// other processors normally called after us to be prevented from running,
		// as we have claimed everything
		if (CLAIM_ALL_ANNOTATIONS) {
			return Collections.singletonList("*");
		}
		else {
			return Collections.singletonList("org.eclipse.jdt.apt.tests.annotations.helloworld.*"); //$NON-NLS-1$
		}
	}
}
