/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 345305 - [compiler][null] Compiler misidentifies a case of "variable can only be null"
 *								bug 392862 - [1.8][compiler][null] Evaluate null annotations on array types
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								bug 403147 - [compiler][null] FUP of bug 400761: consolidate interaction between unboxing, NPE, and deferred checking
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.OperatorOverloadInvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class ArrayReference extends Reference {

	public Expression receiver;
	public Expression position;
	public MethodBinding appropriateMethodForOverload = null;
	public MethodBinding compundAppropriateMethodForOverloda = null;
	public TypeBinding expectedType = null;//Operator overload, for generic function call
	public MethodBinding syntheticAccessor;
	public MethodBinding syntheticCompundAccessor;


	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	public ArrayReference(Expression rec, Expression pos) {
		this.receiver = rec;
		this.position = pos;
		this.sourceStart = rec.sourceStart;
	}

	public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean compoundAssignment) {
		// TODO (maxime) optimization: unconditionalInits is applied to all existing calls
		// account for potential ArrayIndexOutOfBoundsException:
		flowContext.recordAbruptExit();

		if(this.appropriateMethodForOverload != null){
			MethodBinding original = this.appropriateMethodForOverload.original();
			if(original.isPrivate()){
				this.syntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
				currentScope.problemReporter().needToEmulateMethodAccess(original, this);
			}
		}

		if(this.compundAppropriateMethodForOverloda != null){
			MethodBinding original = this.compundAppropriateMethodForOverloda.original();
			if(original.isPrivate()){
				this.syntheticCompundAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
				currentScope.problemReporter().needToEmulateMethodAccess(original, this);
			}
		}
		if (assignment.expression == null) {
			return analyseCode(currentScope, flowContext, flowInfo);
		}
		flowInfo = assignment
				.expression
				.analyseCode(
						currentScope,
						flowContext,
						analyseCode(currentScope, flowContext, flowInfo).unconditionalInits());
		if ((this.resolvedType.tagBits & TagBits.AnnotationNonNull) != 0) {
			int nullStatus = assignment.expression.nullStatus(flowInfo, flowContext);
			if (nullStatus != FlowInfo.NON_NULL) {
				currentScope.problemReporter().nullityMismatch(this, assignment.expression.resolvedType, this.resolvedType, nullStatus, currentScope.environment().getNonNullAnnotationName());
			}
		}
		return flowInfo;
	}

	public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
		if(this.appropriateMethodForOverload != null){
			MethodBinding original = this.appropriateMethodForOverload.original();
			if(original.isPrivate()){
				this.syntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
				currentScope.problemReporter().needToEmulateMethodAccess(original, this);
			}
		}
		if(this.compundAppropriateMethodForOverloda != null){
			MethodBinding original = this.compundAppropriateMethodForOverloda.original();
			if(original.isPrivate()){
				this.syntheticCompundAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
				currentScope.problemReporter().needToEmulateMethodAccess(original, this);
			}
		}
		this.receiver.checkNPE(currentScope, flowContext, flowInfo);
		flowInfo = this.receiver.analyseCode(currentScope, flowContext, flowInfo);
		flowInfo = this.position.analyseCode(currentScope, flowContext, flowInfo);
		this.position.checkNPEbyUnboxing(currentScope, flowContext, flowInfo);
		// account for potential ArrayIndexOutOfBoundsException:
		flowContext.recordAbruptExit();
		return flowInfo;
	}

	public boolean checkNPE(BlockScope scope, FlowContext flowContext, FlowInfo flowInfo) {
		if ((this.resolvedType.tagBits & TagBits.AnnotationNullable) != 0) {
			scope.problemReporter().arrayReferencePotentialNullReference(this);
			return true;
		} else {
			return super.checkNPE(scope, flowContext, flowInfo);
		}
	}

	public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
		if (this.appropriateMethodForOverload != null && this.appropriateMethodForOverload.isValidBinding()) {
			if((this.appropriateMethodForOverload.modifiers & ClassFileConstants.AccStatic) != 0) {
				currentScope.problemReporter().overloadedOperatorMethodNotStatic(this, "[] ="); //$NON-NLS-1$
				return;
			}
			this.generatePutCode(currentScope, codeStream, valueRequired, assignment);
			return;
		}

		int pc = codeStream.position;
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(this.receiver.resolvedType);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		this.position.generateCode(currentScope, codeStream, true);
		assignment.expression.generateCode(currentScope, codeStream, true);
		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
		if (valueRequired) {
			codeStream.generateImplicitConversion(assignment.implicitConversion);
		}
	}

	public void generatePreOverloadAssignment(BlockScope currentScope, CodeStream codeStream, boolean valueRequired){
		if(this.appropriateMethodForOverload != null)
			return;
		int pc = codeStream.position;
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(this.receiver.resolvedType);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		this.position.generateCode(currentScope, codeStream, true);
	}

	public void generatePostOverloadAssignment(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if(this.appropriateMethodForOverload != null)
			return;
		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
	}

	/**
	 * Code generation for a array reference
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		if (this.appropriateMethodForOverload != null && this.appropriateMethodForOverload.isValidBinding()) {
			if((this.appropriateMethodForOverload.modifiers & ClassFileConstants.AccStatic) != 0) {
				currentScope.problemReporter().overloadedOperatorMethodNotStatic(this, "= []"); //$NON-NLS-1$
				return;
			}
			this.generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
			return;
		}
		int pc = codeStream.position;
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(this.receiver.resolvedType);
		}
		this.position.generateCode(currentScope, codeStream, true);
		codeStream.arrayAt(this.resolvedType.id);
		// Generating code for the potential runtime type checking
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		} else {
			boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
			// conversion only generated if unboxing
			if (isUnboxing) codeStream.generateImplicitConversion(this.implicitConversion);
			switch (isUnboxing ? postConversionType(currentScope).id : this.resolvedType.id) {
				case T_long :
				case T_double :
					codeStream.pop2();
					break;
				default :
					codeStream.pop();
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
		if (this.compundAppropriateMethodForOverloda != null) {
			//this.generatePutCode(currentScope, this.appropriateMethodForOverload, this.syntheticAccessor, codeStream, valueRequired, null);
			return;
		}		
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(this.receiver.resolvedType);
		}
		this.position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(this.resolvedType.id);
		int operationTypeID;
		switch(operationTypeID = (this.implicitConversion & TypeIds.IMPLICIT_CONVERSION_MASK) >> 4) {
			case T_JavaLangString :
			case T_JavaLangObject :
			case T_undefined :
				codeStream.generateStringConcatenationAppend(currentScope, null, expression);
				break;
			default :
				// promote the array reference to the suitable operation type
				codeStream.generateImplicitConversion(this.implicitConversion);
				// generate the increment value (will by itself  be promoted to the operation value)
				if (expression == IntLiteral.One) { // prefix operation
					codeStream.generateConstant(expression.constant, this.implicitConversion);
				} else {
					expression.generateCode(currentScope, codeStream, true);
				}
				// perform the operation
				codeStream.sendOperator(operator, operationTypeID);
				// cast the value back to the array reference type
				codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
	}

	public void generatePostIncrement(BlockScope currentScope, CodeStream codeStream, CompoundAssignment postIncrement, boolean valueRequired) {
		MethodBinding mb2 = this.getMethodBindingForOverload(currentScope, new Expression [] {this.position}, false);
		if (mb2.isValidBinding()) {
			currentScope.problemReporter().abortDueToInternalError("Overloaded array reference post increment emulation is not implemented.", this); //$NON-NLS-1$
			return;
		}
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(this.receiver.resolvedType);
		}
		this.position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(this.resolvedType.id);
		if (valueRequired) {
			switch(this.resolvedType.id) {
				case TypeIds.T_long :
				case TypeIds.T_double :
					codeStream.dup2_x2();
					break;
				default :
					codeStream.dup_x2();
					break;
			}
		}
		codeStream.generateImplicitConversion(this.implicitConversion);
		codeStream.generateConstant(
				postIncrement.expression.constant,
				this.implicitConversion);
		codeStream.sendOperator(postIncrement.operator, this.implicitConversion & TypeIds.COMPILE_TYPE_MASK);
		codeStream.generateImplicitConversion(
				postIncrement.preAssignImplicitConversion);
		codeStream.arrayAtPut(this.resolvedType.id, false);
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
		this.receiver.printExpression(0, output).append('[');
		return this.position.printExpression(0, output).append(']');
	}


	public TypeBinding resolveType(BlockScope scope,Expression expression) {
		//Only valid for Assignment
		Assignment assignment;
		try{
			assignment = (Assignment)expression;
		}catch(ClassCastException cce){
			return resolveType(scope);
		}
		MethodBinding mb2 = this.getMethodBindingForOverload(scope, new Expression [] {this.position, assignment.expression}, true);
		if ((mb2 !=null) && (mb2.isValidBinding())) {
			this.resolvedType = TypeBinding.VOID;
			this.setExpectedType(this.resolvedType);
			//Return value will be discarded if exist
			/*if(mb2.returnType != TypeBinding.VOID)
			scope.problemReporter().typeMismatchError(mb2.returnType, TypeBinding.VOID, this, null);*/
			if(this.position.resolvedType == null)
				this.position.resolveType(scope);
			if(assignment.expression == null)
				assignment.expression.resolveType(scope);
			this.receiver.computeConversion(scope, this.receiver.resolvedType, this.receiver.resolvedType);
			this.position.computeConversion(scope, mb2.parameters[0], this.position.resolvedType);
			assignment.expression.computeConversion(scope, mb2.parameters[1], assignment.expression.resolvedType);
			this.appropriateMethodForOverload = mb2;
			if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true))
				scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
			//Return value will be discarded if exist
			return this.resolvedType;
		}

		if(this.receiver == null || this.receiver.resolvedType == null ||  this.position == null || this.position.resolvedType == null
				|| assignment.expression == null || assignment.expression.resolvedType == null){
			return null;
		}

		if(!this.receiver.resolvedType.isArrayType()){	
			scope.problemReporter().referenceMustBeArrayTypeAt(this.receiver.resolvedType, this);
			return null;
		}

		return resolveType(scope);
	}

	public TypeBinding resolveType(BlockScope scope) {
		this.constant = Constant.NotAConstant;
		if (this.receiver instanceof CastExpression	// no cast check for ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression() instanceof NullLiteral) {
			this.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		}

		// test 
		MethodBinding overloadMethod = this.getMethodBindingForOverload(scope, new Expression [] {this.position}, false);
		if ((overloadMethod !=null) && (overloadMethod.isValidBinding())) {
			this.appropriateMethodForOverload = overloadMethod;
			if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true))
				scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
			this.resolvedType = overloadMethod.returnType;
			if(this.position.resolvedType == null)
				this.position.resolveType(scope);
			this.receiver.computeConversion(scope, this.receiver.resolvedType, this.receiver.resolvedType);
			this.position.computeConversion(scope, overloadMethod.parameters[0], this.position.resolvedType);
			this.setExpectedType(this.resolvedType);
			return overloadMethod.returnType;
		} else { // enforce INT
			TypeBinding expectedTypeLocal = TypeBinding.INT;
			this.position.setExpectedType(expectedTypeLocal); // needed in case of generic method invocation
			TypeBinding expressionType = this.position.resolvedType;
			if (expressionType == null) {
				scope.problemReporter().typeMismatchError(TypeBinding.VOID, expectedTypeLocal, this, null);
				return null;
			}	
			if (TypeBinding.notEquals(expressionType, expectedTypeLocal)) {
				if (!expressionType.isCompatibleWith(expectedTypeLocal)) {
					if (!scope.isBoxingCompatibleWith(expressionType, expectedTypeLocal)) {
						scope.problemReporter().typeMismatchError(expressionType, expectedTypeLocal, this, null);
						return null;
					}
				}
			}
		}

		TypeBinding arrayType = this.receiver.resolvedType; //Type(scope);
		if (arrayType != null) {
			this.receiver.computeConversion(scope, arrayType, arrayType);
			if (arrayType.isArrayType()) {
				TypeBinding elementType = ((ArrayBinding) arrayType).elementsType();
				this.resolvedType = ((this.bits & ASTNode.IsStrictlyAssigned) == 0) ? elementType.capture(scope, this.sourceEnd) : elementType;
			} else {
				scope.problemReporter().referenceMustBeArrayTypeAt(arrayType, this);
			}
		}
		TypeBinding positionType = this.position.resolvedType; //TypeExpecting(scope, TypeBinding.INT);

		if (positionType != null) {
			this.position.computeConversion(scope, TypeBinding.INT, positionType);
		}
		return this.resolvedType;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.receiver.traverse(visitor, scope);
			this.position.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}


	public String getMethodName(boolean put) {
		if (put) return "put"; //$NON-NLS-1$
		else return "get"; //$NON-NLS-1$
	}

	public MethodBinding getMethodBindingForOverload(BlockScope scope, Expression [] arguments, boolean put) {
		return getMethodBindingForOverload(scope, arguments, new TypeBinding[0], put);
	}

	public MethodBinding getMethodBindingForOverload(BlockScope scope, Expression [] arguments, TypeBinding[] types, boolean put) {
		//	if (set) return null;

		TypeBinding [] tb_right = new TypeBinding[types.length + arguments.length]; 
		TypeBinding tb_left = null;

		if(this.receiver.resolvedType == null)
			tb_left = this.receiver.resolveType(scope);
		else
			tb_left = this.receiver.resolvedType;

		boolean tbRightValid = true;
		for(int i=0; i<arguments.length; i++){
			if(arguments[i].resolvedType == null)
				tb_right[i] = arguments[i].resolveType(scope);
			else
				tb_right[i] = arguments[i].resolvedType;
			tbRightValid = tbRightValid && (tb_right[i] != null);
		}
		for(int i=0; i<types.length; i++){
			tb_right[arguments.length + i] = types[i];
			tbRightValid = tbRightValid && (tb_right[arguments.length + i] != null);
		}
		final TypeBinding expectedTypeLocal = this.expectedType;
		OperatorOverloadInvocationSite fakeInvocationSite = new OperatorOverloadInvocationSite(){
			public TypeBinding[] genericTypeArguments() { return null; }
			public boolean isSuperAccess(){ return false; }
			public boolean isTypeAccess() { return true; }
			public void setActualReceiverType(ReferenceBinding actualReceiverType) { /* ignore */}
			public void setDepth(int depth) { /* ignore */}
			public void setFieldIndex(int depth){ /* ignore */}
			public int sourceStart() { return 0; }
			public int sourceEnd() { return 0; }
			public TypeBinding getExpectedType() {
				return expectedTypeLocal;
			}
			public TypeBinding expectedType() {
				return getExpectedType();
			}
			@Override
			public TypeBinding invocationTargetType() {
				// TODO Auto-generated method stub
//				throw new RuntimeException("Implement this");
				return null;
			}
			@Override
			public boolean receiverIsImplicitThis() {
				// TODO Auto-generated method stub
				throw new RuntimeException("Implement this");
//				return false;
			}
			@Override
			public InferenceContext18 freshInferenceContext(Scope scope) {
				// TODO Auto-generated method stub
				throw new RuntimeException("Implement this");
//				return null;
			}
			@Override
			public ExpressionContext getExpressionContext() {
				// TODO Auto-generated method stub
				throw new RuntimeException("Implement this");
//				return null;
			}
		};

		String ms = getMethodName(put);
		if ((tb_left == null) || (!tbRightValid)) return null;
		MethodBinding mb2 = scope.getMethod(tb_left, ms.toCharArray(), tb_right,  fakeInvocationSite);
		return mb2;
	}

	public void generateOperatorOverloadCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		this.generatePutCode(currentScope, codeStream, valueRequired, null);
	}

	public void generatePutCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired, Assignment assignment){
		if (this.appropriateMethodForOverload != null){
			this.receiver.generateCode(currentScope, codeStream,true);
			this.position.generateCode(currentScope, codeStream, true);
			if(assignment != null)
				assignment.expression.generateCode(currentScope, codeStream, true);
			if (this.appropriateMethodForOverload.hasSubstitutedParameters() || this.appropriateMethodForOverload.hasSubstitutedReturnType()) { 
				TypeBinding tbo = this.appropriateMethodForOverload.returnType;
				MethodBinding mb3 = this.appropriateMethodForOverload.original();
				MethodBinding final_mb = mb3;
				// TODO remove for real?
				//final_mb.returnType = final_mb.returnType.erasure();
				codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());

				if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
					codeStream.checkcast(tbo);
				}
			} else {
				MethodBinding original = this.appropriateMethodForOverload.original();
				if(original.isPrivate()){
					codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticAccessor, null /* default declaringClass */);
				}
				else{
					codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
				}
				if (!this.appropriateMethodForOverload.returnType.isBaseType()) codeStream.checkcast(this.appropriateMethodForOverload.returnType);
			}
			if (valueRequired) {
				codeStream.generateImplicitConversion(this.implicitConversion);
			}
		}
	}

	public TypeBinding resolveTypeCompundOverloadOperator(BlockScope scope, TypeBinding type) {
		MethodBinding mb1 = getMethodBindingForOverload(scope, new Expression[]{this.position}, new TypeBinding[]{type}, true);
		if(mb1 == null || !mb1.isValidBinding()){
			scope.problemReporter().referenceMustBeArrayTypeAt(this.receiver.resolvedType, this);
			return null;
		}
		this.compundAppropriateMethodForOverloda = mb1;
		if (isMethodUseDeprecated(this.compundAppropriateMethodForOverloda, scope, true))
			scope.problemReporter().deprecatedMethod(this.compundAppropriateMethodForOverloda, this);
		//Return value will be discarded if exist
		return  TypeBinding.VOID;

	}

	public void generatePreCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression,
			int operator, int preAssignImplicitConversion, boolean valueRequired) {
		if (this.compundAppropriateMethodForOverloda != null){
			this.receiver.generateCode(currentScope, codeStream,true);
			this.position.generateCode(currentScope, codeStream, true);
		}

	}

	public void generatePostCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression,
			int operator, int preAssignImplicitConversion, boolean valueRequired) {
		if (this.compundAppropriateMethodForOverloda != null){
			if (this.compundAppropriateMethodForOverloda.hasSubstitutedParameters() || this.compundAppropriateMethodForOverloda.hasSubstitutedReturnType()) { 
				TypeBinding tbo = this.compundAppropriateMethodForOverloda.returnType;
				MethodBinding mb3 = this.compundAppropriateMethodForOverloda.original();
				MethodBinding final_mb = mb3;
				// TODO remove for real?
				//final_mb.returnType = final_mb.returnType.erasure();
				codeStream.invoke((final_mb.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, final_mb, final_mb.declaringClass.erasure());

				if (tbo.erasure().isProvablyDistinct(final_mb.returnType.erasure())) {
					codeStream.checkcast(tbo);
				}
			} else {
				MethodBinding original = this.compundAppropriateMethodForOverloda.original();
				if(original.isPrivate()){
					codeStream.invoke(Opcodes.OPC_invokestatic, this.syntheticCompundAccessor, null /* default declaringClass */);
				}
				else{
					codeStream.invoke((original.declaringClass.isInterface()) ? Opcodes.OPC_invokeinterface : Opcodes.OPC_invokevirtual, original, original.declaringClass);
				}
				if (!this.compundAppropriateMethodForOverloda.returnType.isBaseType()) codeStream.checkcast(this.compundAppropriateMethodForOverloda.returnType);
			}
			if (valueRequired) {
				codeStream.generateImplicitConversion(this.implicitConversion);
			}
			if(!this.compundAppropriateMethodForOverloda.returnType.equals(TypeBinding.VOID)){
				switch (this.compundAppropriateMethodForOverloda.returnType.id) {
					case T_long :
					case T_double :
						codeStream.pop2();
						break;
					case T_void :
						break;
					default :
						codeStream.pop();
				}
			}

		}

	}
}
