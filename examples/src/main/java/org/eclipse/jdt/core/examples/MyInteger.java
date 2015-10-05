package org.eclipse.jdt.core.examples;


public class MyInteger {

	private int value;
	
	public MyInteger(int v) {
		this.value = v;
	}
	
	public int getValue() {
		return this.value;
	}
	
	public MyInteger add(MyInteger i) {
		return new MyInteger(value + i.getValue());
	}
	
	public MyInteger add(int i) {
		return new MyInteger(value + i);
	}
}
