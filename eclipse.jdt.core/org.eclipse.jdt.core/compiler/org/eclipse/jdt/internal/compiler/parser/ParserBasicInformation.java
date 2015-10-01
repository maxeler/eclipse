/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	public final static int

	ERROR_SYMBOL      = 127,
	MAX_NAME_LENGTH   = 41,
	NUM_STATES        = 1156,

	NT_OFFSET         = 127,
	SCOPE_UBOUND      = 308,
	SCOPE_SIZE        = 309,
	LA_STATE_OFFSET   = 17000,
	MAX_LA            = 1,
	NUM_RULES         = 832,
	NUM_TERMINALS     = 127,
	NUM_NON_TERMINALS = 371,
	NUM_SYMBOLS       = 498,
	START_STATE       = 917,
	EOFT_SYMBOL       = 64,
	EOLT_SYMBOL       = 64,
	ACCEPT_ACTION     = 16999,
	ERROR_ACTION      = 17000;
}
