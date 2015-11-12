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

package org.eclipse.jdt.text.tests.performance;


import org.junit.Assert;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;


/**
 * @since 3.1
 */
public class SWTEventHelper {

	public static void pressKeyCode(Display display, int keyCode) {
		pressKeyCode(display, keyCode, true);
	}

	public static void pressKeyCode(Display display, int keyCode, boolean runEventQueue) {
		keyCodeDown(display, keyCode, runEventQueue);
		keyCodeUp(display, keyCode, runEventQueue);
	}

	public static void pressKeyCodeCombination(Display display, int[] keyCodes) {
		pressKeyCodeCombination(display, keyCodes, true);
	}

	public static void pressKeyCodeCombination(Display display, int[] keyCodes, boolean runEventQueue) {
		for (int i= 0; i < keyCodes.length; i++)
			keyCodeDown(display, keyCodes[i], runEventQueue);
		for (int i= keyCodes.length - 1; i >= 0; i--)
			keyCodeUp(display, keyCodes[i], runEventQueue);
	}

	public static void keyCodeDown(Display display, int keyCode) {
		keyCodeEvent(display, SWT.KeyDown, keyCode, true);
	}

	public static void keyCodeDown(Display display, int keyCode, boolean runEventQueue) {
		keyCodeEvent(display, SWT.KeyDown, keyCode, runEventQueue);
	}

	public static void keyCodeUp(Display display, int keyCode) {
		keyCodeEvent(display, SWT.KeyUp, keyCode, true);
	}

	public static void keyCodeUp(Display display, int keyCode, boolean runEventQueue) {
		keyCodeEvent(display, SWT.KeyUp, keyCode, runEventQueue);
	}

	private static Event fgKeyCodeEvent= new Event();
	public static void keyCodeEvent(Display display, int type, int keyCode, boolean runEventQueue) {
		fgKeyCodeEvent.type= type;
		fgKeyCodeEvent.keyCode= keyCode;
		postEvent(display, fgKeyCodeEvent, runEventQueue);
	}

	public static void pressKeyChar(Display display, char keyChar) {
		pressKeyChar(display, keyChar, true);
	}

	public static void pressKeyChar(Display display, char keyChar, boolean runEventQueue) {
		keyCharDown(display, keyChar, runEventQueue);
		keyCharUp(display, keyChar, runEventQueue);
	}

	public static void pressKeyCharCombination(Display display, char[] keyChars) {
		pressKeyCharCombination(display, keyChars, true);
	}

	public static void pressKeyCharCombination(Display display, char[] keyChars, boolean runEventQueue) {
		for (int i= 0; i < keyChars.length; i++)
			keyCharDown(display, keyChars[i], runEventQueue);
		for (int i= keyChars.length - 1; i >= 0; i--)
			keyCharUp(display, keyChars[i], runEventQueue);
	}

	public static void keyCharDown(Display display, char keyChar, boolean runEventQueue) {
		keyCharEvent(display, SWT.KeyDown, keyChar, runEventQueue);
	}

	public static void keyCharUp(Display display, char keyChar, boolean runEventQueue) {
		keyCharEvent(display, SWT.KeyUp, keyChar, runEventQueue);
	}

	private static Event fgKeyCharEvent= new Event();
	public static void keyCharEvent(Display display, int type, char keyChar, boolean runEventQueue) {
		fgKeyCharEvent.type= type;
		fgKeyCharEvent.character= keyChar;
		postEvent(display, fgKeyCharEvent, runEventQueue);
	}

	private static void postEvent(final Display display, final Event event, boolean runEventQueue) {
		DisplayHelper helper= new DisplayHelper() {
			public boolean condition() {
				return display.post(event);
			}
		};
		Assert.assertTrue(helper.waitForCondition(display, 1000));

		if (runEventQueue)
			EditorTestHelper.runEventQueue();

	}

	private static Event fgMouseMoveEvent= new Event();
	public static void mouseMoveEvent(Display display, int x, int y, boolean runEventQueue) {
		fgMouseMoveEvent.type= SWT.MouseMove;
		fgMouseMoveEvent.x= x;
		fgMouseMoveEvent.y= y;
		postEvent(display, fgMouseMoveEvent, runEventQueue);
	}

	public static void mouseDownEvent(Display display, int button, boolean runEventQueue) {
		mouseButtonEvent(display, SWT.MouseDown, button, runEventQueue);
	}

	public static void mouseUpEvent(Display display, int button, boolean runEventQueue) {
		mouseButtonEvent(display, SWT.MouseUp, button, runEventQueue);
	}

	private static Event fgMouseButtonEvent= new Event();
	public static void mouseButtonEvent(Display display, int type, int button, boolean runEventQueue) {
		fgMouseButtonEvent.type= type;
		fgMouseButtonEvent.button= button;
		postEvent(display, fgMouseButtonEvent, runEventQueue);
	}
}
