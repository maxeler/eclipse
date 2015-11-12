/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brock Janiczak (brockj@tpg.com.au)
 *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
 *     Xavier Coulon <xcoulon@redhat.com> - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512 - [JUnit] test method name cut off before (
 *******************************************************************************/

package org.eclipse.jdt.internal.junit.model;

import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestElementContainer;
import org.eclipse.jdt.junit.model.ITestRunSession;

import org.eclipse.core.runtime.Assert;


public abstract class TestElement implements ITestElement {
	public final static class Status {
		public static final Status RUNNING_ERROR= new Status("RUNNING_ERROR", 5); //$NON-NLS-1$
		public static final Status RUNNING_FAILURE= new Status("RUNNING_FAILURE", 6); //$NON-NLS-1$
		public static final Status RUNNING= new Status("RUNNING", 3); //$NON-NLS-1$

		public static final Status ERROR=   new Status("ERROR",   /*1*/ITestRunListener2.STATUS_ERROR); //$NON-NLS-1$
		public static final Status FAILURE= new Status("FAILURE", /*2*/ITestRunListener2.STATUS_FAILURE); //$NON-NLS-1$
		public static final Status OK=      new Status("OK",      /*0*/ITestRunListener2.STATUS_OK); //$NON-NLS-1$
		public static final Status NOT_RUN= new Status("NOT_RUN", 4); //$NON-NLS-1$

		private static final Status[] OLD_CODE= { OK, ERROR, FAILURE};

		private final String fName;
		private final int fOldCode;

		private Status(String name, int oldCode) {
			fName= name;
			fOldCode= oldCode;
		}

		public int getOldCode() {
			return fOldCode;
		}

		public String toString() {
			return fName;
		}

		/* error state predicates */

		public boolean isOK() {
			return this == OK || this == RUNNING || this == NOT_RUN;
		}

		public boolean isFailure() {
			return this == FAILURE || this == RUNNING_FAILURE;
		}

		public boolean isError() {
			return this == ERROR || this == RUNNING_ERROR;
		}

		public boolean isErrorOrFailure() {
			return isError() || isFailure();
		}

		/* progress state predicates */

		public boolean isNotRun() {
			return this == NOT_RUN;
		}

		public boolean isRunning() {
			return this == RUNNING || this == RUNNING_FAILURE || this == RUNNING_ERROR;
		}

		public boolean isDone() {
			return this == OK || this == FAILURE || this == ERROR;
		}

		public static Status combineStatus(Status one, Status two) {
			Status progress= combineProgress(one, two);
			Status error= combineError(one, two);
			return combineProgressAndErrorStatus(progress, error);
		}

		private static Status combineProgress(Status one, Status two) {
			if (one.isNotRun() && two.isNotRun())
				return NOT_RUN;
			else if (one.isDone() && two.isDone())
				return OK;
			else if (!one.isRunning() && !two.isRunning())
				return OK; // one done, one not-run -> a parent failed and its children are not run
			else
				return RUNNING;
		}

		private static Status combineError(Status one, Status two) {
			if (one.isError() || two.isError())
				return ERROR;
			else if (one.isFailure() || two.isFailure())
				return FAILURE;
			else
				return OK;
		}

		private static Status combineProgressAndErrorStatus(Status progress, Status error) {
			if (progress.isDone()) {
				if (error.isError())
					return ERROR;
				if (error.isFailure())
					return FAILURE;
				return OK;
			}

			if (progress.isNotRun()) {
//				Assert.isTrue(!error.isErrorOrFailure());
				return NOT_RUN;
			}

//			Assert.isTrue(progress.isRunning());
			if (error.isError())
				return RUNNING_ERROR;
			if (error.isFailure())
				return RUNNING_FAILURE;
//			Assert.isTrue(error.isOK());
			return RUNNING;
		}

		/**
		 * @param oldStatus one of {@link ITestRunListener2}'s STATUS_* constants
		 * @return the Status
		 */
		public static Status convert(int oldStatus) {
			return OLD_CODE[oldStatus];
		}

		public Result convertToResult() {
			if (isNotRun())
				return Result.UNDEFINED;
			if (isError())
				return Result.ERROR;
			if (isFailure())
				return Result.FAILURE;
			if (isRunning()) {
				return Result.UNDEFINED;
			}
			return Result.OK;
		}

		public ProgressState convertToProgressState() {
			if (isRunning()) {
				return ProgressState.RUNNING;
			}
			if (isDone()) {
				return ProgressState.COMPLETED;
			}
			return ProgressState.NOT_STARTED;
		}

	}

	private final TestSuiteElement fParent;
	private final String fId;
	private String fTestName;

	private Status fStatus;
	private String fTrace;
	private String fExpected;
	private String fActual;

	private boolean fAssumptionFailed;

	/**
	 * Running time in seconds. Contents depend on the current {@link #getProgressState()}:
	 * <ul>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#NOT_STARTED}: {@link Double#NaN}</li>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#RUNNING}: negated start time</li>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#STOPPED}: elapsed time</li>
	 * <li>{@link org.eclipse.jdt.junit.model.ITestElement.ProgressState#COMPLETED}: elapsed time</li>
	 * </ul>
	 */
	/* default */ double fTime= Double.NaN;

	/**
	 * @param parent the parent, can be <code>null</code>
	 * @param id the test id
	 * @param testName the test name
	 */
	public TestElement(TestSuiteElement parent, String id, String testName) {
		Assert.isNotNull(id);
		Assert.isNotNull(testName);
		fParent= parent;
		fId= id;
		fTestName= testName;
		fStatus= Status.NOT_RUN;
		if (parent != null)
			parent.addChild(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.ITestElement#getProgressState()
	 */
	public ProgressState getProgressState() {
		return getStatus().convertToProgressState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.ITestElement#getTestResult()
	 */
	public Result getTestResult(boolean includeChildren) {
		if (fAssumptionFailed) {
			return Result.IGNORED;
		}
		return getStatus().convertToResult();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.ITestElement#getTestRunSession()
	 */
	public ITestRunSession getTestRunSession() {
		return getRoot().getTestRunSession();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.ITestElement#getParentContainer()
	 */
	public ITestElementContainer getParentContainer() {
		if (fParent instanceof TestRoot) {
			return getTestRunSession();
		}
		return fParent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.junit.model.ITestElement#getFailureTrace()
	 */
	public FailureTrace getFailureTrace() {
		Result testResult= getTestResult(false);
		if (testResult == Result.ERROR || testResult == Result.FAILURE
				|| (testResult == Result.IGNORED && fTrace != null)) {
			return new FailureTrace(fTrace, fExpected, fActual);
		}
		return null;
	}

	/**
	 * @return the parent suite, or <code>null</code> for the root
	 */
	public TestSuiteElement getParent() {
		return fParent;
	}

	public String getId() {
		return fId;
	}

	public String getTestName() {
		return fTestName;
	}

	public void setName(String name) {
		fTestName= name;
	}

	public void setStatus(Status status) {
		if (status == Status.RUNNING) {
			fTime= - System.currentTimeMillis() / 1000d ;
		} else if (status.convertToProgressState() == ProgressState.COMPLETED) {
			if (fTime < 0) { // assert ! Double.isNaN(fTime)
				double endTime= System.currentTimeMillis() / 1000.0d;
				fTime= endTime + fTime;
			}
		}

		fStatus= status;
		TestSuiteElement parent= getParent();
		if (parent != null)
			parent.childChangedStatus(this, status);
	}

	public void setStatus(Status status, String trace, String expected, String actual) {
		if (trace != null && fTrace != null) {
			//don't overwrite first trace if same test run logs multiple errors
			fTrace= fTrace + trace;
		} else {
			fTrace= trace;
			fExpected= expected;
			fActual= actual;
		}
		setStatus(status);
	}

	public Status getStatus() {
		return fStatus;
	}

	public String getTrace() {
		return fTrace;
	}

	public String getExpected() {
		return fExpected;
	}

	public String getActual() {
		return fActual;
	}

	public boolean isComparisonFailure() {
		return fExpected != null && fActual != null;
	}

	/**
	 * @return return the class name
	 * @see org.eclipse.jdt.internal.junit.runner.ITestIdentifier#getName()
	 * @see org.eclipse.jdt.internal.junit.runner.MessageIds#TEST_IDENTIFIER_MESSAGE_FORMAT
	 */
	public String getClassName() {
		return extractClassName(getTestName());
	}

	private static String extractClassName(String testNameString) {
		testNameString= extractRawClassName(testNameString);
		testNameString= testNameString.replace('$', '.'); // see bug 178503
		return testNameString;
	}

	public static String extractRawClassName(String testNameString) {
		if (testNameString.startsWith("[") && testNameString.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
			// a group of parameterized tests, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=102512
			return testNameString;
		}
		int index= testNameString.lastIndexOf('(');
		if (index < 0)
			return testNameString;
		int end= testNameString.lastIndexOf(')');
		testNameString= testNameString.substring(index + 1, end > index ? end : testNameString.length());
		return testNameString;
	}

	public TestRoot getRoot() {
		return getParent().getRoot();
	}

	public void setElapsedTimeInSeconds(double time) {
		fTime= time;
	}

	public double getElapsedTimeInSeconds() {
		if (Double.isNaN(fTime) || fTime < 0.0d) {
			return Double.NaN;
		}

		return fTime;
	}

	public void setAssumptionFailed(boolean assumptionFailed) {
		fAssumptionFailed= assumptionFailed;
	}

	public boolean isAssumptionFailure() {
		return fAssumptionFailed;
	}

	public String toString() {
		return getProgressState() + " - " + getTestResult(true); //$NON-NLS-1$
	}
}
