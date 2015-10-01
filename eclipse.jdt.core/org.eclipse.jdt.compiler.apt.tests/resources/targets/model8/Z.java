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

public class Z <@Type("tp1") K, @Type("tp2") V> {
	Z<@Type("ta1") String, @Type("ta2") Object> z1 = null;
	public <T> Z(@Type("parameter") T t){}
	public <@Type("mp1") T, @Type("mp2") U> void foo() {}
}
