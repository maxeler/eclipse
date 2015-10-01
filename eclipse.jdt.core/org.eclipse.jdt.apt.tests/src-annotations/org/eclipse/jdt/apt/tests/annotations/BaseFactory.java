/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.sun.mirror.apt.AnnotationProcessorFactory;

/**
 * Base class for annotation factories. Takes care of supported
 * annotations and supported options.
 */
public abstract class BaseFactory implements AnnotationProcessorFactory {

	private final Collection<String> _supportedAnnotations;
	
	/**
	 * Pass in supported annotations. At least one is required,
	 * the rest are optional.
	 * 
	 * @param anno main annotation
	 * @param otherAnnos other supported annotations. Not necessary if 
	 * no extra annotations are supported.
	 */
	public BaseFactory(String anno, String... otherAnnos) {
		_supportedAnnotations = new ArrayList<String>(1 + otherAnnos.length);
		_supportedAnnotations.add(anno);
		for (String s : otherAnnos) {
			_supportedAnnotations.add(s);
		}
	}
	
	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

	public Collection<String> supportedAnnotationTypes() {
		return _supportedAnnotations;
	}

}
