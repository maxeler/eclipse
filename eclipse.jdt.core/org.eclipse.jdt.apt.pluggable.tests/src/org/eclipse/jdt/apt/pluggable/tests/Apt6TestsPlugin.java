/*******************************************************************************
 * Copyright (c) 2007-2009 BEA Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     BEA Systems, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.pluggable.tests;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Apt6TestsPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.jdt.apt.pluggable.tests"; //$NON-NLS-1$
	
	private static Apt6TestsPlugin _thePlugin = null;

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		_thePlugin = this;
	}

	public Apt6TestsPlugin() {
		// TODO Auto-generated constructor stub
	}
	
	public static Apt6TestsPlugin thePlugin() {
		return _thePlugin;
	}

}
