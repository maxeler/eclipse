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
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								bug 402993 - [null] Follow up of bug 401088: Missing warning about redundant null check
 *     Jesper S Moller - Contributions for
 *								bug 382721 - [1.8][compiler] Effectively final variables needs special treatment
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompoundAssignment extends Assignment implements OperatorIds {
	public int operator;
	public int preAssignImplicitConversion;
	public MethodBinding appropriateMethodForOverload = null;
	public MethodBinding syntheticAccessor = null;
	public TypeBinding expectedType = null;//Operator overload, for generic function call

	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}
	

	//  var op exp is equivalent to var = (varType) var op exp
	// assignmentImplicitConversion stores the cast needed for the assignment

	public CompoundAssignment(Expression lhs, Expression expression,int operator, int sourceEnd) {
		//lhs is always a reference by construction ,
		//but is build as an expression ==> the checkcast cannot fail

		super(lhs, expression, sourceEnd);
		lhs.bits &= ~IsStrictlyAssigned; // tag lhs as NON assigned - it is also a read access
		lhs.bits |= IsCompoundAssigned; // tag lhs as assigned by compound
		this.operator = operator ;
	}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext,
		FlowInfo flowInfo) {
		
		if(this.appropriateMethodForOverload != null){
			MethodBinding original = this.appropriateMethodForOverload.original();
			if(original.isPrivate()){
				this.syntheticAccessor = ((SourceTypeBinding)original.declaringClass).addSyntheticMethod(original, false /* not super access there */);
				currentScope.problemReporter().needToEmulateMethodAccess(original, this);
			}
		}

	// record setting a variable: various scenarii are possible, setting an array reference,
	// a field reference, a blank final field reference, a field of an enclosing instance or
	// just a local variable.
		if (this.resolvedType != null && this.resolvedType.id != T_JavaLangString) {
		this.lhs.checkNPE(currentScope, flowContext, flowInfo);
		// account for exceptions thrown by any arithmetics:
		flowContext.recordAbruptExit();
	}
	flowInfo = ((Reference) this.lhs).analyseAssignment(currentScope, flowContext, flowInfo, this, true).unconditionalInits();
	if (this.resolvedType.id == T_JavaLangString) {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=339250
		LocalVariableBinding local = this.lhs.localVariableBinding();
		if (local != null) {
			// compound assignment results in a definitely non null value for String
			flowInfo.markAsDefinitelyNonNull(local);
			flowContext.markFinallyNullStatus(local, FlowInfo.NON_NULL);
		}
	}
	return flowInfo;
}

	public boolean checkCastCompatibility() {
		return true;
	}
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

		// various scenarii are possible, setting an array reference,
		// a field reference, a blank final field reference, a field of an enclosing instance or
		// just a local variable.

		int pc = codeStream.position;
		if (this.appropriateMethodForOverload != null){
			Reference ref = (Reference)this.lhs;
			if(ref instanceof ArrayReference){
				((ArrayReference)ref).generatePreCompoundAssignment(currentScope, codeStream, this.expression, this.operator, this.preAssignImplicitConversion, valueRequired);
			}
			if(ref instanceof CompositeArrayReference){
				((CompositeArrayReference)ref).generatePreCompoundAssignment(currentScope, codeStream, this.expression, this.operator, this.preAssignImplicitConversion, valueRequired);
			}
			ref.generatePreOverloadAssignment(currentScope, codeStream, valueRequired);
			this.lhs.generateCode(currentScope, codeStream,true);//for local invoke
			this.expression.generateCode(currentScope, codeStream, true);
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
			ref.generatePostOverloadAssignment(currentScope, codeStream, valueRequired);
			if(ref instanceof ArrayReference){
				codeStream.generateImplicitConversion(this.implicitConversion);
				((ArrayReference)ref).generatePostCompoundAssignment(currentScope, codeStream, this.expression, this.operator, this.preAssignImplicitConversion, valueRequired);
			}
			if(ref instanceof CompositeArrayReference){
				codeStream.generateImplicitConversion(this.implicitConversion);
				((CompositeArrayReference)ref).generatePostCompoundAssignment(currentScope, codeStream, this.expression, this.operator, this.preAssignImplicitConversion, valueRequired);
			}
		}else{
		 ((Reference) this.lhs).generateCompoundAssignment(currentScope, codeStream, this.expression, this.operator, this.preAssignImplicitConversion, valueRequired);
		}
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

public int nullStatus(FlowInfo flowInfo, FlowContext flowContext) {
	return FlowInfo.NON_NULL;
	// we may have complained on checkNPE, but we avoid duplicate error
}

public String getBindingMethodName() {
	switch (this.operator) {
		case PLUS :
			return "add"; //$NON-NLS-1$
		case MINUS :
			return "sub"; //$NON-NLS-1$
		case MULTIPLY :
			return "mul"; //$NON-NLS-1$
		case DIVIDE :
			return "div"; //$NON-NLS-1$
		case REMAINDER :
			return "mod"; //$NON-NLS-1$
		case AND :
			return "and"; //$NON-NLS-1$
		case OR :
			return "or"; //$NON-NLS-1$
		case XOR :
			return "xor"; //$NON-NLS-1$
		case LEFT_SHIFT:
			return "shiftLeft"; //$NON-NLS-1$
		case RIGHT_SHIFT:
			return "shiftRight"; //$NON-NLS-1$
		case UNSIGNED_RIGHT_SHIFT :
			return "unsignedShiftRight"; //$NON-NLS-1$
	}
	return ""; //$NON-NLS-1$
}

	public String operatorToString() {
		switch (this.operator) {
			case PLUS :
				return "+="; //$NON-NLS-1$
			case MINUS :
				return "-="; //$NON-NLS-1$
			case MULTIPLY :
				return "*="; //$NON-NLS-1$
			case DIVIDE :
				return "/="; //$NON-NLS-1$
			case AND :
				return "&="; //$NON-NLS-1$
			case OR :
				return "|="; //$NON-NLS-1$
			case XOR :
				return "^="; //$NON-NLS-1$
			case REMAINDER :
				return "%="; //$NON-NLS-1$
			case LEFT_SHIFT :
				return "<<="; //$NON-NLS-1$
			case RIGHT_SHIFT :
				return ">>="; //$NON-NLS-1$
			case UNSIGNED_RIGHT_SHIFT :
				return ">>>="; //$NON-NLS-1$
		}
		return "unknown operator"; //$NON-NLS-1$
	}

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		this.lhs.printExpression(indent, output).append(' ').append(operatorToString()).append(' ');
		return this.expression.printExpression(0, output) ;
	}

	public boolean isOverloadedArray(Expression e){
		if(e instanceof Reference){
			if(e instanceof CompositeArrayReference)
				return true;
			if(e instanceof ArrayReference){
				ArrayReference ref = (ArrayReference) e;
				if(!ref.receiver.resolvedType.isArrayType())
					return true;
			}
		}
		return false;
	}
	
	public TypeBinding resolveType(BlockScope scope) {
		this.constant = Constant.NotAConstant;
		if (!(this.lhs instanceof Reference) || this.lhs.isThis()) {
			scope.problemReporter().expressionShouldBeAVariable(this.lhs);
			return null;
		}
		boolean expressionIsCast = this.expression instanceof CastExpression;
		if (expressionIsCast)
			this.expression.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		TypeBinding originalLhsType = this.lhs.resolveType(scope);
		TypeBinding originalExpressionType = this.expression.resolveType(scope);
		if (originalLhsType == null || originalExpressionType == null)
			return null;

		TypeBinding typeCompundOverloadOperator = null;
		MethodBinding mb2 = null;
		if (!originalLhsType.isBoxingType() && !originalLhsType.isBaseType() && !originalLhsType.isStringType()){
			mb2 = this.getMethodBindingForOverload(scope, originalLhsType, originalExpressionType);
			if (mb2 != null){
				this.appropriateMethodForOverload = mb2;
				if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true))
					scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
				if(isOverloadedArray(this.lhs)){
					//Compound on arrays or put(add(get(index)));
					typeCompundOverloadOperator = ((Reference)this.lhs).resolveTypeCompundOverloadOperator(scope, this.appropriateMethodForOverload.returnType);
					if( typeCompundOverloadOperator == null){
						//No put
						return null;
					}else{
						if(!typeCompundOverloadOperator.equals(TypeBinding.VOID)){
							scope.problemReporter().typeMismatchError(typeCompundOverloadOperator, TypeBinding.VOID, this.lhs, null);
							return null;
						}
					}
				}
			}
			else
				return null;
		}
		if(!originalLhsType.isStringType()){
			if ((!originalExpressionType.isBaseType() && !originalExpressionType.isBoxingType() && !originalExpressionType.isStringType()) 
					|| originalExpressionType.id == TypeBinding.NULL.id){
				if (mb2 != null){
					if(this instanceof PostfixExpression || this instanceof PrefixExpression){
						scope.problemReporter().invalidOperator(this, originalLhsType, originalExpressionType);
						return null;			
					}
					this.lhs.computeConversion(scope, this.lhs.resolvedType, this.lhs.resolvedType);
					this.expression.computeConversion(scope, mb2.parameters[0], this.expression.resolvedType);
					return this.resolvedType = mb2.returnType;
				}else{
					scope.problemReporter().invalidOperator(this, originalLhsType, originalExpressionType);
					return null;
				}			
			}
		}
		if(mb2 != null && mb2.isValidBinding()){
			if(this instanceof PostfixExpression || this instanceof PrefixExpression){
				scope.problemReporter().invalidOperator(this, originalLhsType, originalExpressionType);
				return null;			
			}
			this.lhs.computeConversion(scope, this.lhs.resolvedType, this.lhs.resolvedType);
			this.expression.computeConversion(scope, mb2.parameters[0], this.expression.resolvedType);
			
			if(typeCompundOverloadOperator != null){
				return typeCompundOverloadOperator;
			}
			return this.resolvedType = mb2.returnType;
		}

		// autoboxing support
		LookupEnvironment env = scope.environment();
		TypeBinding lhsType = originalLhsType, expressionType = originalExpressionType;
		boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		boolean unboxedLhs = false;
		if (use15specifics) {
			if (!lhsType.isBaseType() && !expressionType.isStringType() && expressionType.id != T_null) {
				TypeBinding unboxedType = env.computeBoxingType(lhsType);
				if (TypeBinding.notEquals(unboxedType, lhsType)) {
					lhsType = unboxedType;
					unboxedLhs = true;
				}
			}
			if (!expressionType.isBaseType() && !lhsType.isStringType()  && lhsType.id != T_null) {
				expressionType = env.computeBoxingType(expressionType);
			}
		}
		int lhsID = lhsType.id;
		int expressionID = expressionType.id;
		if (lhsID > 15 || expressionID > 15) {
			if (lhsID != T_JavaLangString) { // String += Thread is valid whereas Thread += String  is not
				scope.problemReporter().invalidOperator(this, lhsType, expressionType);
				return null;
			}
			expressionID = T_JavaLangObject; // use the Object has tag table
		}

		// the code is an int
		// (cast)  left   Op (cast)  rigth --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8     <<4        <<0

		// the conversion is stored INTO the reference (info needed for the code gen)
		int result = OperatorExpression.OperatorSignatures[this.operator][ (lhsID << 4) + expressionID];
		if (result == T_undefined && (mb2 == null || !mb2.isValidBinding())) {
			scope.problemReporter().invalidOperator(this, lhsType, expressionType);
			return null;
		}
		if (this.operator == PLUS){
			if(lhsID == T_JavaLangObject && (scope.compilerOptions().complianceLevel < ClassFileConstants.JDK1_7)) {
				// <Object> += <String> is illegal (39248) for compliance < 1.7
				scope.problemReporter().invalidOperator(this, lhsType, expressionType);
				return null;
			} else {
				// <int | boolean> += <String> is illegal
				if ((lhsType.isNumericType() || lhsID == T_boolean) && !expressionType.isNumericType()){
					scope.problemReporter().invalidOperator(this, lhsType, expressionType);
					return null;
				}
			}
		}
		TypeBinding resultType = TypeBinding.wellKnownType(scope, result & 0x0000F);
		if (checkCastCompatibility()) {
			if (originalLhsType.id != T_JavaLangString && resultType.id != T_JavaLangString) {
				if (!checkCastTypesCompatibility(scope, originalLhsType, resultType, null)) {
					scope.problemReporter().invalidOperator(this, originalLhsType, expressionType);
					return null;
				}
			}
		}
		if(mb2 == null || !mb2.isValidBinding()){
		this.lhs.computeConversion(scope, TypeBinding.wellKnownType(scope, (result >>> 16) & 0x0000F), originalLhsType);
		this.expression.computeConversion(scope, TypeBinding.wellKnownType(scope, (result >>> 8) & 0x0000F), originalExpressionType);
		}
		this.preAssignImplicitConversion =  (unboxedLhs ? BOXING : 0) | (lhsID << 4) | (result & 0x0000F);
		if (unboxedLhs) scope.problemReporter().autoboxing(this, lhsType, originalLhsType);
		if (expressionIsCast)
			CastExpression.checkNeedForArgumentCasts(scope, this.operator, result, this.lhs, originalLhsType.id, false, this.expression, originalExpressionType.id, true);
		return this.resolvedType = originalLhsType;
	}

	public MethodBinding getMethodBindingForOverload(BlockScope scope, TypeBinding left, TypeBinding right) {

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
				throw new RuntimeException("Implement this");
//				return null;
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

		String ms = getBindingMethodName();
		MethodBinding mb2;
		//right is class
		if (!right.isBoxingType() && !right.isBaseType()){
			mb2 = scope.getMethod(left, ms.toCharArray(), new TypeBinding[]{right},  fakeInvocationSite);
			if(mb2 != null && mb2.isValidBinding()){
				if((mb2.modifiers & ClassFileConstants.AccStatic) != 0) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, getBindingMethodName());
					return null;
				}
				if(TypeBinding.notEquals(mb2.returnType, left)){
					scope.problemReporter().invalidOperator(this, left, right);// TODO needs to generate different error
					return null;
				}
				return mb2;
			}
			scope.problemReporter().invalidOperator(this, left, right);
			return null;
		}
		if (right.isBoxingType() || right.isBaseType()){
			mb2 = scope.getMethod(left, ms.toCharArray(), new TypeBinding[]{right}, fakeInvocationSite);
			if(mb2 != null && mb2.isValidBinding()){
				if((mb2.modifiers & ClassFileConstants.AccStatic) != 0) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, getBindingMethodName());
					return null;
				}
				if(TypeBinding.notEquals(mb2.returnType, left)){
					scope.problemReporter().invalidOperator(this, left, right);// TODO needs to generate different error
					return null;
				}
				return mb2;
			}
			/*if(mb2 instanceof ProblemMethodBinding && (mb2.problemId() == ProblemReasons.NotFound || mb2.problemId() == ProblemReasons.NotVisible) ){
				ProblemMethodBinding problemBinding = (ProblemMethodBinding)mb2;
				if(problemBinding.closestMatch.isValidBinding())
					return problemBinding.closestMatch;
			}*/
		}
		scope.problemReporter().invalidOperator(this, left, right);// TODO needs to generate different error
		return null;		 
	}

	public boolean restrainUsageToNumericTypes(){
		return false ;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.lhs.traverse(visitor, scope);
			this.expression.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
