/*******************************************************************************
 * Copyright (c) 2007 - 2009 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.model.pb;

import targets.model.pa.AnnoZ;
import org.eclipse.jdt.compiler.apt.tests.annotations.TypedAnnos.AnnoArrayString;

@AnnoZ(annoZString = "annoZOnD")
public class D extends AB {
	public enum DEnum { DEnum1, DEnum2, DEnum3 }
	
	@AnnoZ(annoZint = 31, annoZString = "annoZOnDMethod")
	public void methodDvoid(DEnum dEnum1) {}
	
	@AnnoZ(annoZString = "annoZOnDMethod2", annoZint = 12)
	public void methodDvoid2() {}
	
	@AnnoArrayString(value = "methodDvoid3Value")
	public void methodDvoid3() {}
}

// Should inherit AnnoZ
class DChild extends D {}