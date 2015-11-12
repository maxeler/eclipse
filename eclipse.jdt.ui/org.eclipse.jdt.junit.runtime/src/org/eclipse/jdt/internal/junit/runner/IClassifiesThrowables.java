/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Saff (saff@mit.edu) - initial API and implementation
 *             (bug 102632: [JUnit] Support for JUnit 4.)
 *******************************************************************************/

package org.eclipse.jdt.internal.junit.runner;

public interface IClassifiesThrowables {

	public abstract boolean isComparisonFailure(Throwable throwable);

	/**
	 * @param t a {@link Throwable}
	 * @return the stack trace for the given {@link Throwable}.
	 */
	public abstract String getTrace(Throwable t);

}
