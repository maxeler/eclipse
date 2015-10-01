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

import org.eclipse.jdt.compiler.apt.tests.annotations.Type;
import org.eclipse.jdt.compiler.apt.tests.annotations.Type$1;
import org.eclipse.jdt.compiler.apt.tests.annotations.Type.One;

@Type("c")
public class X extends @Type("s") Object implements @Type("i1") I, @Type("i2") J {
	@Type("f") String _field = null;
	@Type("f1") X _field1 = null;
	@Type("f1") X _field11 = null;
	@Type$1 @One String _field2 = null;
	X _field3 = null;
	X. @Type("xy") XY xy;
	int _i = 10;
	public void noAnnotationHere() {
	}
	@Deprecated @Type("m") String foo() {
		return null;
	}
	void bar(@Type("p1") String p1, @Type("p2") String p2) {}
	public void bar2(@Type("receiver") X this) {}
	// Static methods and top level constructors do not have receivers
	public static void main(String[] args) {}
	public X(){}
	class XY {
		XY(@Type("receiver") X X.this) {}
	}
}

interface I {}
interface J {}
