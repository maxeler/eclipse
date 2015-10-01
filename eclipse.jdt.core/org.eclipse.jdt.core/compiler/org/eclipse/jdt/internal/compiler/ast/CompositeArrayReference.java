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
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * 
 * @author milan
 *
 */
public class CompositeArrayReference extends Reference{

	public Expression receiver;
	public Expression positionOne;
	public Expression positionTwo;
	public MethodBinding appropriateMethodForOverload = null;
	public MethodBinding compundAppropriateMethodForOverloda = null;
	public TypeBinding expectedType = null;//Operator overload, for generic function call
	public MethodBinding syntheticAccessor;
	public MethodBinding syntheticCompundAccessor;

	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	public CompositeArrayReference(Expression rec, Expression pos1, Expression pos2) {
		this.receiver = rec;
		this.positionOne = pos1;
		this.positionTwo = pos2;
		this.sourceStart = rec.sourceStart;
	}
	
	public FlowInfo analyseAssignment(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo, Assignment assignment, boolean compoundAssignment) {
		// TODO (maxime) optimization: unconditionalInits is applied to all existing calls
		
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
		return assignment
			.expression
			.analyseCode(
				currentScope,
				flowContext,
				analyseCode(currentScope, flowContext, flowInfo).unconditionalInits());
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
		flowInfo = this.positionOne.analyseCode(currentScope, flowContext, flowInfo);
		return this.positionTwo.analyseCode(currentScope, flowContext, flowInfo);
	}

	public void generateAssignment(BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired) {
		MethodBinding mb2 = this.getMethodBindingForOverload(currentScope, new Expression[]{this.positionOne, this.positionTwo, assignment.expression}, true);
		if (mb2 != null && mb2.isValidBinding()) {
			if((mb2.modifiers & ClassFileConstants.AccStatic) != 0) {
				currentScope.problemReporter().overloadedOperatorMethodNotStatic(this, "[:] ="); //$NON-NLS-1$
				return;
			}
			this.generatePutCode(currentScope, codeStream, valueRequired, assignment);
			return;
		}
		currentScope.problemReporter().invalidOrMissingOverloadedOperator(this, "put", this.positionOne.resolvedType, this.positionTwo.resolvedType, assignment.expression.resolvedType); //$NON-NLS-1$
		return;
		
//		int pc = codeStream.position;
//		this.receiver.generateCode(currentScope, codeStream, true);
//		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
//				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
//			codeStream.checkcast(this.receiver.resolvedType);
//		}
//		codeStream.recordPositionsFrom(pc, this.sourceStart);
//		this.positionOne.generateCode(currentScope, codeStream, true);
//		this.positionTwo.generateCode(currentScope, codeStream, true);
//		assignment.expression.generateCode(currentScope, codeStream, true);
//		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
//		if (valueRequired) {
//			codeStream.generateImplicitConversion(assignment.implicitConversion);
//		}
	}
	
	public void generatePreOverloadAssignment(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		 // this cannot be assigned
	}
	public void generatePostOverloadAssignment(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		 // this cannot be assigned
	}
	/**
	 * Code generation for a array reference
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		//MethodBinding mb2 = this.getMethodBindingForOverload(currentScope, new Expression[]{this.positionOne, this.positionTwo}, false);
		if (this.appropriateMethodForOverload != null && this.appropriateMethodForOverload.isValidBinding()) {
			if((this.appropriateMethodForOverload.modifiers & ClassFileConstants.AccStatic) != 0) {
				currentScope.problemReporter().overloadedOperatorMethodNotStatic(this, "= [:]"); //$NON-NLS-1$
				return;
			}
			this.generateOperatorOverloadCode(currentScope, codeStream, valueRequired);
			return;
		}
		currentScope.problemReporter().invalidOrMissingOverloadedOperator(this, "get", this.positionOne.resolvedType, this.positionTwo.resolvedType); //$NON-NLS-1$
		return;

//		int pc = codeStream.position;
//		this.receiver.generateCode(currentScope, codeStream, true);
//		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
//				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
//			codeStream.checkcast(this.receiver.resolvedType);
//		}
//		this.positionOne.generateCode(currentScope, codeStream, true);
//		this.positionTwo.generateCode(currentScope, codeStream, true);
//		codeStream.arrayAt(this.resolvedType.id);
//		// Generating code for the potential runtime type checking
//		if (valueRequired) {
//			codeStream.generateImplicitConversion(this.implicitConversion);
//		} else {
//			boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
//			// conversion only generated if unboxing
//			if (isUnboxing) codeStream.generateImplicitConversion(this.implicitConversion);
//			switch (isUnboxing ? postConversionType(currentScope).id : this.resolvedType.id) {
//				case T_long :
//				case T_double :
//					codeStream.pop2();
//					break;
//				default :
//					codeStream.pop();
//			}
//		}
//		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void generateCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression, int operator, int assignmentImplicitConversion, boolean valueRequired) {
		if (this.compundAppropriateMethodForOverloda != null) {
			return;
		}		
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(this.receiver.resolvedType);
		}
		this.positionOne.generateCode(currentScope, codeStream, true);
		this.positionTwo.generateCode(currentScope, codeStream, true);
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
		MethodBinding mb2 = this.getMethodBindingForOverload(currentScope, new Expression[]{this.positionOne, this.positionTwo}, false);
		if (mb2.isValidBinding()) {
			currentScope.problemReporter().abortDueToInternalError("Overloaded array reference post increment emulation is not implemented.", this); //$NON-NLS-1$
			return;
		}
		this.receiver.generateCode(currentScope, codeStream, true);
		if (this.receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(this.receiver.resolvedType);
		}
		this.positionOne.generateCode(currentScope, codeStream, true);
		this.positionTwo.generateCode(currentScope, codeStream, true);
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

	public int nullStatus(FlowInfo flowInfo) {
		return FlowInfo.UNKNOWN;
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
		this.receiver.printExpression(0, output).append('[');
		this.positionOne.printExpression(0, output).append(":"); //$NON-NLS-1$
		return this.positionTwo.printExpression(0, output).append(']');
	}

public TypeBinding resolveType(BlockScope scope, Expression expression) {
	//Only valid for Assignment
	Assignment assignment;
	try{
		assignment = (Assignment)expression;
	}catch(ClassCastException cce){
		return resolveType(scope);
	}
	MethodBinding mb2 = this.getMethodBindingForOverload(scope, new Expression [] {this.positionOne, this.positionTwo, assignment.expression}, true);
	if ((mb2 !=null) && (mb2.isValidBinding())) {
		this.resolvedType = TypeBinding.VOID;
		this.setExpectedType(this.resolvedType);
		this.appropriateMethodForOverload = mb2;
		if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true))
			scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
		/*if(mb2.returnType != TypeBinding.VOID)
			scope.problemReporter().typeMismatchError(mb2.returnType, TypeBinding.VOID, this, null);*/
		if(this.positionOne.resolvedType == null)
			this.positionOne.resolveType(scope);
		if(this.positionTwo.resolvedType == null)
			this.positionTwo.resolveType(scope);
		if(assignment.expression == null)
			assignment.expression.resolveType(scope);
		this.receiver.computeConversion(scope, this.receiver.resolvedType, this.receiver.resolvedType);
		this.positionOne.computeConversion(scope, mb2.parameters[0], this.positionOne.resolvedType);
		this.positionTwo.computeConversion(scope, mb2.parameters[1], this.positionTwo.resolvedType);
		assignment.expression.computeConversion(scope, mb2.parameters[2], assignment.expression.resolvedType);
		return this.resolvedType;
	}
	
	if(this.receiver == null || this.receiver.resolvedType == null ||  this.positionOne == null || this.positionOne.resolvedType == null
			||  this.positionTwo == null || this.positionTwo.resolvedType == null || assignment.expression == null || assignment.expression.resolvedType == null){
		return null;
	}
	
	scope.problemReporter().referenceMustBeArrayTypeAt(this.receiver.resolvedType, this);
	return null;
}

	public TypeBinding resolveType(BlockScope scope) {
		this.constant = Constant.NotAConstant;
		if (this.receiver instanceof CastExpression	// no cast check for ((type[])null)[0]
				&& ((CastExpression)this.receiver).innermostCastedExpression() instanceof NullLiteral) {
			this.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		}
		
		// test 
		MethodBinding overloadMethod = this.getMethodBindingForOverload(scope, new Expression[]{this.positionOne, this.positionTwo}, false);
		if ((overloadMethod !=null) && (overloadMethod.isValidBinding())) {
			this.appropriateMethodForOverload = overloadMethod;
			if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true))
				scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
			this.resolvedType = overloadMethod.returnType;
			this.setExpectedType(this.resolvedType);
			if(this.positionOne.resolvedType == null)
				this.positionOne.resolveType(scope);
			if(this.positionTwo.resolvedType == null)
				this.positionTwo.resolveType(scope);
			this.receiver.computeConversion(scope, this.receiver.resolvedType, this.receiver.resolvedType);
			this.positionOne.computeConversion(scope, overloadMethod.parameters[0], this.positionOne.resolvedType);
			this.positionTwo.computeConversion(scope, overloadMethod.parameters[1], this.positionTwo.resolvedType);
			return overloadMethod.returnType;
		} else { // enforce INT
			TypeBinding expectedTypeLocal = TypeBinding.INT;
			this.positionOne.setExpectedType(expectedTypeLocal); // needed in case of generic method invocation
			TypeBinding expressionTypeOne = this.positionOne.resolvedType;
			this.positionTwo.setExpectedType(expectedTypeLocal); // needed in case of generic method invocation
			TypeBinding expressionTypeTwo = this.positionTwo.resolvedType;
			if (expressionTypeOne == null) {
				scope.problemReporter().typeMismatchError(TypeBinding.VOID, expectedTypeLocal, this, null);
				return null;
			}
			if (expressionTypeTwo == null) {
				scope.problemReporter().typeMismatchError(TypeBinding.VOID, expectedTypeLocal, this, null);
				return null;
			}	
			if (TypeBinding.notEquals(expressionTypeOne, expectedTypeLocal)) {
				if (!expressionTypeOne.isCompatibleWith(expectedTypeLocal)) {
					if (!scope.isBoxingCompatibleWith(expressionTypeOne, expectedTypeLocal)) {
						scope.problemReporter().typeMismatchError(expressionTypeOne, expectedTypeLocal, this, null);
						return null;
					}
				}
			}
			if (TypeBinding.notEquals(expressionTypeTwo, expectedTypeLocal)) {
				if (!expressionTypeTwo.isCompatibleWith(expectedTypeLocal)) {
					if (!scope.isBoxingCompatibleWith(expressionTypeTwo, expectedTypeLocal)) {
						scope.problemReporter().typeMismatchError(expressionTypeTwo, expectedTypeLocal, this, null);
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
		TypeBinding positionTypeOne = this.positionOne.resolvedType; //TypeExpecting(scope, TypeBinding.INT);
		TypeBinding positionTypeTwo = this.positionTwo.resolvedType; //TypeExpecting(scope, TypeBinding.INT);
		
		if (positionTypeOne != null) {
			this.positionOne.computeConversion(scope, TypeBinding.INT, positionTypeOne);
		}
		if (positionTypeTwo != null) {
			this.positionTwo.computeConversion(scope, TypeBinding.INT, positionTypeTwo);
		}

		return this.resolvedType;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			this.receiver.traverse(visitor, scope);
			this.positionOne.traverse(visitor, scope);
			this.positionTwo.traverse(visitor, scope);
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
		TypeBinding [] tb_right = new TypeBinding[arguments.length + types.length]; 
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

		String ms = getMethodName(put);
		
		if ((tb_left == null) || (!tbRightValid)) return null;
		
		MethodBinding mb2 = scope.getMethod(tb_left, ms.toCharArray(), tb_right,  fakeInvocationSite);
//		if (!mb2.isValidBinding()) {
//			mb2 = scope.getMethod(tb_right, (ms+"AsRHS").toCharArray(), new TypeBinding[]{tb_left},  fakeInvocationSite);	//$NON-NLS-1$	
//		}

		return mb2;
	}

	public void generatePutCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired, Assignment assignment){
		if (this.appropriateMethodForOverload != null){
			this.receiver.generateCode(currentScope, codeStream,true);
			this.positionOne.generateCode(currentScope, codeStream, true);
			this.positionTwo.generateCode(currentScope, codeStream, true);
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
	
	public void generatePreCompoundAssignment(BlockScope currentScope, CodeStream codeStream, Expression expression,
			int operator, int preAssignImplicitConversion, boolean valueRequired) {
		if (this.compundAppropriateMethodForOverloda != null){
			this.receiver.generateCode(currentScope, codeStream,true);
			this.positionOne.generateCode(currentScope, codeStream, true);
			this.positionTwo.generateCode(currentScope, codeStream, true);
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

	public void generateOperatorOverloadCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		this.generatePutCode(currentScope, codeStream, valueRequired, null);
	}

	public TypeBinding resolveTypeCompundOverloadOperator(BlockScope scope, TypeBinding type) {
		MethodBinding mb1 = getMethodBindingForOverload(scope, new Expression[]{this.positionOne, this.positionTwo}, new TypeBinding[]{type}, true);
		if(mb1 == null || !mb1.isValidBinding()){
			scope.problemReporter().referenceMustBeArrayTypeAt(this.receiver.resolvedType, this);
			return null;
		}
		TypeBinding returnType = mb1.returnType;
		this.compundAppropriateMethodForOverloda = mb1;
		if (isMethodUseDeprecated(this.compundAppropriateMethodForOverloda, scope, true))
			scope.problemReporter().deprecatedMethod(this.compundAppropriateMethodForOverloda, this);
		//Return value will be discarded if exist
		return  TypeBinding.VOID;


	}

	public void generateCompundOverloadAssignment() {
		// TODO Auto-generated method stub
		
	}
}
