package org.eclipse.jdt.internal.codeassist.complete;

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public class EmptyAnnotationDeclaration extends TypeDeclaration {

	char[][] annotationNames;

	public EmptyAnnotationDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public void setAnnotationNames(char[][] annotations) {
		this.annotationNames = annotations;
	}

	public char[][] getAnnotationNames() {
		return this.annotationNames;
	}
}
