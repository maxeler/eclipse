/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.text.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.test.performance.PerformanceMeter;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Measures the time to comment/uncomment a large compilation unit.
 *
 * @since 3.1
 */
public class ToggleCommentTest extends TextPerformanceTestCase {

	private static final Class THIS= ToggleCommentTest.class;

	private static final String FILE= PerformanceTestSetup.STYLED_TEXT;

	private static final int WARM_UP_RUNS= 3;

	private static final int MEASURED_RUNS= 3;

	private ITextEditor fEditor;

	public static Test suite() {
		return new PerformanceTestSetup(new TestSuite(THIS));
	}

	protected void setUp() throws Exception {
		super.setUp();
		fEditor= (ITextEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(FILE), true);
		runAction(fEditor.getAction(ITextEditorActionConstants.SELECT_ALL));
		setWarmUpRuns(WARM_UP_RUNS);
		setMeasuredRuns(MEASURED_RUNS);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		EditorTestHelper.closeAllEditors();
	}

	/**
	 * Measures the time to comment/uncomment a large compilation unit.
	 */
	public void testToggleComment2() {
		measureToggleComment(getNullPerformanceMeter(), getNullPerformanceMeter(), getWarmUpRuns());
		PerformanceMeter commentMeter= createPerformanceMeter("-comment");
		PerformanceMeter uncommentMeter= createPerformanceMeter("-uncomment");
		measureToggleComment(commentMeter, uncommentMeter, getMeasuredRuns());
		commitAllMeasurements();
		assertAllPerformance();
	}

	private void measureToggleComment(PerformanceMeter commentMeter, PerformanceMeter uncommentMeter, int runs) {
		IAction toggleComment= fEditor.getAction("ToggleComment");
		for (int i= 0; i < runs; i++) {
			commentMeter.start();
			runAction(toggleComment);
			commentMeter.stop();
			EditorTestHelper.runEventQueue(5000);
			uncommentMeter.start();
			runAction(toggleComment);
			uncommentMeter.stop();
			EditorTestHelper.runEventQueue(5000);
		}
	}

	private void runAction(IAction action) {
		action.run();
		EditorTestHelper.runEventQueue();
	}
}
