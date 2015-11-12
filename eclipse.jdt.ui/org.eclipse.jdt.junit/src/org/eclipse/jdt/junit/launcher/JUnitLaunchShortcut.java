/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     David Saff (saff@mit.edu) - bug 102632: [JUnit] Support for JUnit 4.
 *******************************************************************************/
package org.eclipse.jdt.junit.launcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import org.eclipse.core.resources.IResource;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut2;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.junit.launcher.AssertionVMArg;
import org.eclipse.jdt.internal.junit.launcher.ITestKind;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitMigrationDelegate;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;
import org.eclipse.jdt.internal.junit.ui.JUnitMessages;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.util.ExceptionHandler;
import org.eclipse.jdt.internal.junit.util.TestSearchEngine;

import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jdt.ui.JavaUI;

import org.eclipse.jdt.internal.ui.actions.SelectionConverter;


/**
 * The launch shortcut to launch JUnit tests.
 *
 * <p>
 * This class may be instantiated and subclassed.
 * </p>
 * @since 3.3
 */
public class JUnitLaunchShortcut implements ILaunchShortcut2 {

	private static final String EMPTY_STRING= ""; //$NON-NLS-1$

	/**
	 * Default constructor.
	 */
	public JUnitLaunchShortcut() {
	}


	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
	 */
	public void launch(IEditorPart editor, String mode) {
		ITypeRoot element= JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
		if (element != null) {
			IMethod selectedMethod= resolveSelectedMethodName(editor, element);
			if (selectedMethod != null) {
				launch(new Object[] { selectedMethod }, mode);
			} else {
				launch(new Object[] { element }, mode);
			}
		} else {
			showNoTestsFoundDialog();
		}
	}

	private IMethod resolveSelectedMethodName(IEditorPart editor, ITypeRoot element) {
		try {
			ISelectionProvider selectionProvider= editor.getSite().getSelectionProvider();
			if (selectionProvider == null)
				return null;

			ISelection selection= selectionProvider.getSelection();
			if (!(selection instanceof ITextSelection))
				return null;

			ITextSelection textSelection= (ITextSelection) selection;

			IJavaElement elementAtOffset= SelectionConverter.getElementAtOffset(element, textSelection);
			if (! (elementAtOffset instanceof IMethod))
				return null;

			IMethod method= (IMethod) elementAtOffset;

			ISourceRange nameRange= method.getNameRange();
			if (nameRange.getOffset() <= textSelection.getOffset()
					&& textSelection.getOffset() + textSelection.getLength() <= nameRange.getOffset() + nameRange.getLength())
				return method;
		} catch (JavaModelException e) {
			// ignore
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			launch(((IStructuredSelection) selection).toArray(), mode);
		} else {
			showNoTestsFoundDialog();
		}
	}

	private void launch(Object[] elements, String mode) {
		try {
			IJavaElement elementToLaunch= null;

			if (elements.length == 1) {
				Object selected= elements[0];
				if (!(selected instanceof IJavaElement) && selected instanceof IAdaptable) {
					selected= ((IAdaptable) selected).getAdapter(IJavaElement.class);
				}
				if (selected instanceof IJavaElement) {
					IJavaElement element= (IJavaElement) selected;
					switch (element.getElementType()) {
						case IJavaElement.JAVA_PROJECT:
						case IJavaElement.PACKAGE_FRAGMENT_ROOT:
						case IJavaElement.PACKAGE_FRAGMENT:
						case IJavaElement.TYPE:
						case IJavaElement.METHOD:
							elementToLaunch= element;
							break;
						case IJavaElement.CLASS_FILE:
							elementToLaunch= ((IClassFile) element).getType();
							break;
						case IJavaElement.COMPILATION_UNIT:
							elementToLaunch= findTypeToLaunch((ICompilationUnit) element, mode);
							break;
					}
				}
			}
			if (elementToLaunch == null) {
				showNoTestsFoundDialog();
				return;
			}
			performLaunch(elementToLaunch, mode);
		} catch (InterruptedException e) {
			// OK, silently move on
		} catch (CoreException e) {
			ExceptionHandler.handle(e, getShell(), JUnitMessages.JUnitLaunchShortcut_dialog_title, JUnitMessages.JUnitLaunchShortcut_message_launchfailed);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), JUnitMessages.JUnitLaunchShortcut_dialog_title, JUnitMessages.JUnitLaunchShortcut_message_launchfailed);
		}
	}

	private void showNoTestsFoundDialog() {
		MessageDialog.openInformation(getShell(), JUnitMessages.JUnitLaunchShortcut_dialog_title, JUnitMessages.JUnitLaunchShortcut_message_notests);
	}

	private IType findTypeToLaunch(ICompilationUnit cu, String mode) throws InterruptedException, InvocationTargetException {
		IType[] types= findTypesToLaunch(cu);
		if (types.length == 0) {
			return null;
		} else if (types.length > 1) {
			return chooseType(types, mode);
		}
		return types[0];
	}

	private IType[] findTypesToLaunch(ICompilationUnit cu) throws InterruptedException, InvocationTargetException {
		ITestKind testKind= TestKindRegistry.getContainerTestKind(cu);
		return TestSearchEngine.findTests(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), cu, testKind);
	}

	private void performLaunch(IJavaElement element, String mode) throws InterruptedException, CoreException {
		ILaunchConfigurationWorkingCopy temparary= createLaunchConfiguration(element);
		ILaunchConfiguration config= findExistingLaunchConfiguration(temparary, mode);
		if (config == null) {
			// no existing found: create a new one
			config= temparary.doSave();
		}
		DebugUITools.launch(config, mode);
	}

	private IType chooseType(IType[] types, String mode) throws InterruptedException {
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_POST_QUALIFIED));
		dialog.setElements(types);
		dialog.setTitle(JUnitMessages.JUnitLaunchShortcut_dialog_title2);
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage(JUnitMessages.JUnitLaunchShortcut_message_selectTestToDebug);
		} else {
			dialog.setMessage(JUnitMessages.JUnitLaunchShortcut_message_selectTestToRun);
		}
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			return (IType) dialog.getFirstResult();
		}
		throw new InterruptedException(); // cancelled by user
	}

	private Shell getShell() {
		return JUnitPlugin.getActiveWorkbenchShell();
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Show a selection dialog that allows the user to choose one of the
	 * specified launch configurations. Return the chosen config, or
	 * <code>null</code> if the user cancelled the dialog.
	 *
	 * @param configList list of {@link ILaunchConfiguration}s
	 * @param mode launch mode
	 * @return ILaunchConfiguration
	 * @throws InterruptedException if cancelled by the user
	 */
	private ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList, String mode) throws InterruptedException {
		IDebugModelPresentation labelProvider= DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setElements(configList.toArray());
		dialog.setTitle(JUnitMessages.JUnitLaunchShortcut_message_selectConfiguration);
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			dialog.setMessage(JUnitMessages.JUnitLaunchShortcut_message_selectDebugConfiguration);
		} else {
			dialog.setMessage(JUnitMessages.JUnitLaunchShortcut_message_selectRunConfiguration);
		}
		dialog.setMultipleSelection(false);
		int result= dialog.open();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		throw new InterruptedException(); // cancelled by user
	}

	/**
	 * Returns the launch configuration type id of the launch configuration this shortcut will create. Clients can override this method to
	 * return the id of their launch configuration.
	 *
	 * @return the launch configuration type id of the launch configuration this shortcut will create
	 */
	protected String getLaunchConfigurationTypeId() {
		return JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION;
	}

	/**
	 * Creates a launch configuration working copy for the given element. The launch configuration type created will be of the type returned by {@link #getLaunchConfigurationTypeId}.
	 * The element type can only be of type {@link IJavaProject}, {@link IPackageFragmentRoot}, {@link IPackageFragment}, {@link IType} or {@link IMethod}.
	 *
	 * Clients can extend this method (should call super) to configure additional attributes on the launch configuration working copy.
	 * @param element element to launch
	 *
	 * @return a launch configuration working copy for the given element
	 * @throws CoreException if creation failed
	 */
	protected ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element) throws CoreException {
		final String testName;
		final String mainTypeQualifiedName;
		final String containerHandleId;

		switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT:
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT: {
				String name= JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_FULLY_QUALIFIED);
				containerHandleId= element.getHandleIdentifier();
				mainTypeQualifiedName= EMPTY_STRING;
				testName= name.substring(name.lastIndexOf(IPath.SEPARATOR) + 1);
			}
			break;
			case IJavaElement.TYPE: {
				containerHandleId= EMPTY_STRING;
				mainTypeQualifiedName= ((IType) element).getFullyQualifiedName('.'); // don't replace, fix for binary inner types
				testName= element.getElementName();
			}
			break;
			case IJavaElement.METHOD: {
				IMethod method= (IMethod) element;
				containerHandleId= EMPTY_STRING;
				mainTypeQualifiedName= method.getDeclaringType().getFullyQualifiedName('.');
				testName= method.getDeclaringType().getElementName() + '.' + method.getElementName();
			}
			break;
			default:
				throw new IllegalArgumentException("Invalid element type to create a launch configuration: " + element.getClass().getName()); //$NON-NLS-1$
		}

		String testKindId= TestKindRegistry.getContainerTestKindId(element);

		ILaunchConfigurationType configType= getLaunchManager().getLaunchConfigurationType(getLaunchConfigurationTypeId());
		ILaunchConfigurationWorkingCopy wc= configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(testName));

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeQualifiedName);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, element.getJavaProject().getElementName());
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, containerHandleId);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, testKindId);
		JUnitMigrationDelegate.mapResources(wc);
		AssertionVMArg.setArgDefault(wc);
		if (element instanceof IMethod) {
			wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, element.getElementName()); // only set for methods
		}
		return wc;
	}

	/**
	 * Returns the attribute names of the attributes that are compared when looking for an existing similar launch configuration.
	 * Clients can override and replace to customize.
	 *
	 * @return the attribute names of the attributes that are compared
	 */
	protected String[] getAttributeNamesToCompare() {
		return new String[] {
			IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER,
			IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME
		};
	}

	private static boolean hasSameAttributes(ILaunchConfiguration config1, ILaunchConfiguration config2, String[] attributeToCompare) {
		try {
			for (String element : attributeToCompare) {
				String val1= config1.getAttribute(element, EMPTY_STRING);
				String val2= config2.getAttribute(element, EMPTY_STRING);
				if (!val1.equals(val2)) {
					return false;
				}
			}
			return true;
		} catch (CoreException e) {
			// ignore access problems here, return false
		}
		return false;
	}


	private ILaunchConfiguration findExistingLaunchConfiguration(ILaunchConfigurationWorkingCopy temporary, String mode) throws InterruptedException, CoreException {
		List<ILaunchConfiguration> candidateConfigs= findExistingLaunchConfigurations(temporary);

		// If there are no existing configs associated with the IType, create
		// one.
		// If there is exactly one config associated with the IType, return it.
		// Otherwise, if there is more than one config associated with the
		// IType, prompt the
		// user to choose one.
		int candidateCount= candidateConfigs.size();
		if (candidateCount == 0) {
			return null;
		} else if (candidateCount == 1) {
			return candidateConfigs.get(0);
		} else {
			// Prompt the user to choose a config. A null result means the user
			// cancelled the dialog, in which case this method returns null,
			// since cancelling the dialog should also cancel launching
			// anything.
			ILaunchConfiguration config= chooseConfiguration(candidateConfigs, mode);
			if (config != null) {
				return config;
			}
		}
		return null;
	}

	private List<ILaunchConfiguration> findExistingLaunchConfigurations(ILaunchConfigurationWorkingCopy temporary) throws CoreException {
		ILaunchConfigurationType configType= temporary.getType();

		ILaunchConfiguration[] configs= getLaunchManager().getLaunchConfigurations(configType);
		String[] attributeToCompare= getAttributeNamesToCompare();

		ArrayList<ILaunchConfiguration> candidateConfigs= new ArrayList<ILaunchConfiguration>(configs.length);
		for (ILaunchConfiguration config : configs) {
			if (hasSameAttributes(config, temporary, attributeToCompare)) {
				candidateConfigs.add(config);
			}
		}
		return candidateConfigs;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				return findExistingLaunchConfigurations(ss.getFirstElement());
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public ILaunchConfiguration[] getLaunchConfigurations(final IEditorPart editor) {
		final ITypeRoot element= JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
		if (element != null) {
			IMethod selectedMethod = null;
			if (Display.getCurrent() == null) {
				final IMethod[] temp = new IMethod[1];
				Runnable runnable= new Runnable() {
					public void run() {
						temp[0]= resolveSelectedMethodName(editor, element);
					}
				};
				Display.getDefault().syncExec(runnable);
				selectedMethod = temp[0];
			} else {
				selectedMethod= resolveSelectedMethodName(editor, element);
			}
			Object candidate = element;
			if (selectedMethod != null) {
				candidate = selectedMethod;
			}
			return findExistingLaunchConfigurations(candidate);
		}
		return null;
	}

	private ILaunchConfiguration[] findExistingLaunchConfigurations(Object candidate) {
		if (!(candidate instanceof IJavaElement) && candidate instanceof IAdaptable) {
			candidate= ((IAdaptable) candidate).getAdapter(IJavaElement.class);
		}
		if (candidate instanceof IJavaElement) {
			IJavaElement element= (IJavaElement) candidate;
			IJavaElement elementToLaunch = null;
			try {
				switch (element.getElementType()) {
					case IJavaElement.JAVA_PROJECT:
					case IJavaElement.PACKAGE_FRAGMENT_ROOT:
					case IJavaElement.PACKAGE_FRAGMENT:
					case IJavaElement.TYPE:
					case IJavaElement.METHOD:
						elementToLaunch= element;
						break;
					case IJavaElement.CLASS_FILE:
						elementToLaunch= ((IClassFile) element).getType();
						break;
					case IJavaElement.COMPILATION_UNIT:
						elementToLaunch= ((ICompilationUnit) element).findPrimaryType();
						break;
				}
				if (elementToLaunch == null) {
					return null;
				}
				ILaunchConfigurationWorkingCopy workingCopy= createLaunchConfiguration(elementToLaunch);
				List<ILaunchConfiguration> list= findExistingLaunchConfigurations(workingCopy);
				return list.toArray(new ILaunchConfiguration[list.size()]);
			} catch (CoreException e) {
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public IResource getLaunchableResource(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss= (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object selected= ss.getFirstElement();
				if (!(selected instanceof IJavaElement) && selected instanceof IAdaptable) {
					selected= ((IAdaptable) selected).getAdapter(IJavaElement.class);
				}
				if (selected instanceof IJavaElement) {
					return ((IJavaElement)selected).getResource();
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @since 3.4
	 */
	public IResource getLaunchableResource(IEditorPart editor) {
		ITypeRoot element= JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
		if (element != null) {
			try {
				return element.getCorrespondingResource();
			} catch (JavaModelException e) {
			}
		}
		return null;
	}
}
