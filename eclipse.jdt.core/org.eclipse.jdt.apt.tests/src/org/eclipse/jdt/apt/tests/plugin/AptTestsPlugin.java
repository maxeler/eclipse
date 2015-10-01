/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.plugin;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class AptTestsPlugin extends Plugin {

	public AptTestsPlugin() {
		_default = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start( context );
	}

	public void stop(BundleContext context) throws Exception {
		super.stop( context );
	}

	public static AptTestsPlugin getDefault() {
		return _default;
	}

	private static AptTestsPlugin	_default;

}
