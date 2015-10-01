package org.eclipse.jdt.internal.compiler.flow;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;

/**
 * 
 * @author milan
 *
 */
public class SWITCHFlowContext extends FlowContext {

	public BranchLabel breakLabel;
	public UnconditionalFlowInfo initsOnBreak = FlowInfo.DEAD_END;

public SWITCHFlowContext(FlowContext parent, ASTNode associatedNode, BranchLabel breakLabel) {
	super(parent, associatedNode);
	this.breakLabel = breakLabel;
}

public BranchLabel breakLabel() {
	return this.breakLabel;
}

public String individualToString() {
	StringBuffer buffer = new StringBuffer("SWITCH flow context"); //$NON-NLS-1$
	buffer.append("[initsOnBreak -").append(this.initsOnBreak.toString()).append(']'); //$NON-NLS-1$
	return buffer.toString();
}

public boolean isBreakable() {
	return false;
}

public void recordBreakFrom(FlowInfo flowInfo) {
	if ((this.initsOnBreak.tagBits & FlowInfo.UNREACHABLE) == 0) {
		this.initsOnBreak = this.initsOnBreak.mergedWith(flowInfo.unconditionalInits());
	}
	else {
		this.initsOnBreak = flowInfo.unconditionalCopy();
	}
}
}
