/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.annotations;

/**
 * This annotation may be processed to generate a non-Java resource,
 * using the Filer.createResource() API
 */
public @interface GenResource {
	
	/**
	 * package location in which to generate the file, relative to generated file output location.
	 */
	String pkg() default "";
	
	/**
	 * path of resource file to generate, relative to pkg.
	 */
	String relativeName();
	
	/**
	 * content of generated resource, or empty string to use binaryContent
	 */
	String stringContent() default "";
	
	/**
	 * content of generated resource, if stringContent is empty
	 */
	byte[] binaryContent() default {};
	
}
