/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.negative.pa;

/**
 * This code is syntactic but contains semantic errors due to missing types.
 * All the M* types (M for "Missing") are expected to be unresolved.
 * The desired behavior is specified in the javadoc for package 
 * javax.lang.model.element: in general, missing types should be replaced
 * by empty types with the same name.
 */
class Negative3 {
	M1 foo(M2.M3.M4 param) {}
}

interface I2 extends MI1 {
	M5 boo(M6.M7.M8 param);
}