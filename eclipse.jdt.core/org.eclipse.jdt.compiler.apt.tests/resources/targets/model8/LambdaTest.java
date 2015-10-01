/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package targets.model8;

public class LambdaTest {
	String foo() {
		return null;
	}
}     

interface DefaultInterface {
	public default String defaultMethod () {
		return null;
	}
	default int anotherDefault() {
		return 0;
	}
	public static String staticMethod () {
		return null;
	}
}     

interface FunctionalInterface {
	public abstract String abstractMethod ();
}
