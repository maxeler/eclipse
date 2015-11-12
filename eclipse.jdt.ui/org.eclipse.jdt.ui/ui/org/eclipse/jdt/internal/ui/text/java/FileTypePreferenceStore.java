package org.eclipse.jdt.internal.ui.text.java;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

public class FileTypePreferenceStore implements IPreferenceStore {

	private String FileType = JavaModelUtil.DEFAULT_CU_SUFFIX;
	
	public final static String KEYNAME = "FILE_TYPE"; //$NON-NLS-1$
	
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
	}

	public boolean contains(String name) {

		if(name.equals(KEYNAME))
			return true;
		return false;
	}

	public void firePropertyChangeEvent(String name, Object oldValue,
			Object newValue) {
	}

	public boolean getBoolean(String name) {
		return false;
	}

	public boolean getDefaultBoolean(String name) {
		return false;
	}

	public double getDefaultDouble(String name) {
		return 0;
	}

	public float getDefaultFloat(String name) {
		return 0;
	}

	public int getDefaultInt(String name) {
		return 0;
	}

	public long getDefaultLong(String name) {
		return 0;
	}

	public String getDefaultString(String name) {
		return null;
	}

	public double getDouble(String name) {
		return 0;
	}

	public float getFloat(String name) {
		return 0;
	}

	public int getInt(String name) {
		return 0;
	}

	public long getLong(String name) {
		return 0;
	}

	public String getString(String name) {
		if(name.equals(KEYNAME))
			return this.FileType;
		return ""; //$NON-NLS-1$
	}

	public boolean isDefault(String name) {
		return false;
	}

	public boolean needsSaving() {
		return false;
	}

	public void putValue(String name, String value) {

	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {

	}

	public void setDefault(String name, double value) {

	}

	public void setDefault(String name, float value) {

	}

	public void setDefault(String name, int value) {

	}

	public void setDefault(String name, long value) {

	}

	public void setDefault(String name, String defaultObject) {
		if(name.equals(KEYNAME))
			this.FileType = defaultObject;
	}

	public void setDefault(String name, boolean value) {

	}

	public void setToDefault(String name) {

	}

	public void setValue(String name, double value) {

	}

	public void setValue(String name, float value) {

	}

	public void setValue(String name, int value) {

	}

	public void setValue(String name, long value) {

	}

	public void setValue(String name, String value) {
		if(name.equals(KEYNAME))
			this.FileType = value;

	}

	public void setValue(String name, boolean value) {

	}

}
