/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
import org.eclipse.jdt.compiler.apt.tests.annotations.InheritedAnno;

@InheritedAnno
@NotInheritedAnno
public class InheritanceA {
	@InheritedAnno
	@NotInheritedAnno
	public InheritanceA() {}
	
	@InheritedAnno
	@NotInheritedAnno
	public InheritanceA(int i) {}
	
	// Not a constructor: has a return value
	@InheritedAnno
	@NotInheritedAnno
	public void InheritanceA() {}
	
	@InheritedAnno
	@NotInheritedAnno
	public class AChild {}
	
	public class ANotAnnotated {}
	
	@InheritedAnno
	@NotInheritedAnno
	public interface AIntf {}
	
	@InheritedAnno
	@NotInheritedAnno
	public void foo() {}
	
	@InheritedAnno
	@NotInheritedAnno
	public int i;
	
	@InheritedAnno
	@NotInheritedAnno
	public enum AEnum { A, B }
}

class InheritanceB extends InheritanceA {
	public class BChild extends AChild {}
	
	public class BNotAnnotated extends ANotAnnotated {}
	
	public interface BIntf extends AIntf {}
	
	public void foo() {}
	
	public int i;
}