package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.codegen.Opcodes;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.InferenceContext18;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.OperatorOverloadInvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class ConnectCompoundAssignment extends Assignment implements OperatorIds{

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

	public ConnectCompoundAssignment(Expression lhs, Expression expression,int operator, int sourceEnd) {
		//lhs is always a reference by construction ,
		//but is build as an expression ==> the checkcast cannot fail

		super(lhs, expression, sourceEnd);
		lhs.bits &= ~IsStrictlyAssigned; // tag lhs as NON assigned - it is also a read access
		lhs.bits |= IsCompoundAssigned; // tag lhs as assigned by compound
		this.operator = operator ;
		this.constant = Constant.NotAConstant;
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
		this.lhs.checkNPE(currentScope, flowContext, flowInfo);
		flowInfo = this.lhs.analyseCode(currentScope, flowContext, flowInfo);
		return this.expression.analyseCode(currentScope, flowContext, flowInfo);
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {

		// various scenarii are possible, setting an array reference,
		// a field reference, a blank final field reference, a field of an enclosing instance or
		// just a local variable.
		int pc = codeStream.position;	
		if (this.appropriateMethodForOverload != null){
			this.lhs.generateCode(currentScope, codeStream,true);
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
		}else{
			((Reference) this.lhs).generateCompoundAssignment(currentScope, codeStream, this.expression, this.operator, this.preAssignImplicitConversion, valueRequired);
		}
		
		if (valueRequired) {
			codeStream.generateImplicitConversion(this.implicitConversion);
		}else {
			boolean isUnboxing = (this.implicitConversion & TypeIds.UNBOXING) != 0;
			// conversion only generated if unboxing
			if (isUnboxing) codeStream.generateImplicitConversion(this.implicitConversion);
			if (this.appropriateMethodForOverload != null){
				MethodBinding mb3 = this.appropriateMethodForOverload.original();
				/**
				 * When in loop, return type is unchanged, while its always 'void' outside a loop
				 */
				switch (isUnboxing ? postConversionType(currentScope).id : mb3.returnType.id) {
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
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.NON_NULL;
	// we may have complained on checkNPE, but we avoid duplicate error
}

public String getBindingMethodName() {
	return "connect"; //$NON-NLS-1$
}

	public String operatorToString() {
		return "<=="; //$NON-NLS-1$
	}

	public StringBuffer printExpressionNoParenthesis(int indent, StringBuffer output) {

		this.lhs.printExpression(indent, output).append(' ').append(operatorToString()).append(' ');
		return this.expression.printExpression(0, output) ;
	}

	public TypeBinding resolveType(BlockScope scope) {
		
		TypeBinding originalLhsType = this.lhs.resolveType(scope);
		TypeBinding originalExpressionType = this.expression.resolveType(scope);
		
		/*if(this.lhs.resolvedType.isBaseType() || this.lhs.resolvedType.isBoxingType() || this.lhs.resolvedType.isStringType()){
			scope.problemReporter().invalidOperator(this, this.lhs.resolvedType, this.expression.resolvedType);
			return null;
		}*/

		
		if(originalLhsType == null || originalExpressionType == null)
			return null;
		MethodBinding mb2 = this.getMethodBindingForOverload(scope, originalLhsType, originalExpressionType);
		if ((mb2 !=null) && (mb2.isValidBinding())) {
			this.resolvedType = TypeBinding.VOID;
//			this.setExpectedType(this.resolvedType);
//			if(mb2.returnType != TypeBinding.VOID)
//				scope.problemReporter().typeMismatchError(mb2.returnType, TypeBinding.VOID, this, null);
			this.lhs.computeConversion(scope, this.lhs.resolvedType, this.lhs.resolvedType);
			this.expression.computeConversion(scope, mb2.parameters[0], originalExpressionType);
//			if(mb2.returnType == TypeBinding.VOID){
				this.appropriateMethodForOverload = mb2;
				if (isMethodUseDeprecated(this.appropriateMethodForOverload, scope, true))
					scope.problemReporter().deprecatedMethod(this.appropriateMethodForOverload, this);
				this.resolvedType = mb2.returnType;
				return this.resolvedType;
//			}
		}
		//scope.problemReporter().invalidOperator(this, originalLhsType, originalExpressionType);
		return null;
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
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, assignmentToString());
					return null;
				}
//				if(mb2.returnType != TypeBinding.VOID){
//					scope.problemReporter().typeMismatchError(mb2.returnType, TypeBinding.VOID, this, null);
//					return null;
//				}
				return mb2;
			}
			scope.problemReporter().invalidOperator(this, left, right);
			return null;
		}
		if (right.isBoxingType() || right.isBaseType()){
			mb2 = scope.getMethod(left, ms.toCharArray(), new TypeBinding[]{right}, fakeInvocationSite);
			if(mb2 != null && mb2.isValidBinding()){
				if((mb2.modifiers & ClassFileConstants.AccStatic) != 0) {
					scope.problemReporter().overloadedOperatorMethodNotStatic(this, assignmentToString());
					return null;
				}
//				if(mb2.returnType != TypeBinding.VOID){
//					scope.problemReporter().typeMismatchError(mb2.returnType, TypeBinding.VOID, this, null);
//					return null;
//				}else{
					return mb2;
//				}
			}
		}
		scope.problemReporter().invalidOrMissingOverloadedOperator(this, this.expression.resolvedType);// TODO needs to generate different error
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

	public String assignmentToString() {
		return "connect"; //$NON-NLS-1$
	}
}
