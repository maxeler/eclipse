/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.declaration; 

import java.lang.annotation.Annotation;
import java.util.Collection;

import org.eclipse.jdt.apt.core.internal.env.BaseProcessorEnv;
import org.eclipse.jdt.apt.core.internal.util.SourcePositionImpl;
import org.eclipse.jdt.core.dom.*;

import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;

public abstract class MemberDeclarationImpl extends DeclarationImpl implements MemberDeclaration, EclipseMirrorObject
{
    MemberDeclarationImpl(final IBinding binding, BaseProcessorEnv env)
    {
        super(binding, env);
    }
    
    public void accept(DeclarationVisitor visitor)
    {
        visitor.visitMemberDeclaration(this);
    }
    
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
    {
		final IAnnotationBinding[] instances = getAnnotationInstances();
		return _getAnnotation(annotationClass, instances);
    }

    public Collection<AnnotationMirror> getAnnotationMirrors()
    {
		final IAnnotationBinding[] instances = getAnnotationInstances();
		return _getAnnotationMirrors(instances);		
    }
	
	private IAnnotationBinding[] getAnnotationInstances()
	{
		final IBinding binding = getDeclarationBinding();
		final IAnnotationBinding[] instances;
		switch( binding.getKind() )
		{
		case IBinding.TYPE:
			instances = ((ITypeBinding)binding).getAnnotations();
			break;
		case IBinding.METHOD:
			instances = ((IMethodBinding)binding).getAnnotations();
			break;
		case IBinding.VARIABLE:
			instances = ((IVariableBinding)binding).getAnnotations();
			break;
		case IBinding.PACKAGE:
			// TODO: support package annotation
			return null;
		default:			
			throw new IllegalStateException();
		}
		return instances;
	}

    public String getDocComment()
    {
        if( isFromSource()){
        	final ASTNode node = getAstNode();        	
        	if(node != null){
        		if( node instanceof BodyDeclaration )
        			
        			return getDocComment((BodyDeclaration)node);

        		else if( node.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT ){
        			final ASTNode parent = node.getParent();
        			// a field declaration
        			if( parent instanceof BodyDeclaration )
        				return getDocComment((BodyDeclaration)parent);
        			
        		}
        		return ""; //$NON-NLS-1$
        	}  
        }
        return null;
    }    

	/**
	 * @return the source position of this declaration. 
	 *         Return null if this declaration did not come from source or 
	 *         if the declaration is (or is part of) a secondary type that is defined 
	 *         outside of the file associated with the environment.
	 */
    public SourcePosition getPosition()
    {
        if( isFromSource() ){
			final ASTNode node = getRangeNode();
			if( node == null ) return null;			       
            final CompilationUnit unit = getCompilationUnit();
            final int start = node.getStartPosition();    		
            return new SourcePositionImpl(start,
					node.getLength(),
					unit.getLineNumber(start),
					unit.getColumnNumber(start),
					this);
        }
        return null;
    }   
} 
