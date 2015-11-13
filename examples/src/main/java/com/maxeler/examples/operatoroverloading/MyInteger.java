package com.maxeler.examples.operatoroverloading;


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
	
	public MyInteger addAsRHS(int i) {
		return this + i;
	}

	public MyInteger add(float f) {
		return new MyInteger(value + (int)f);
	}
	
	public MyInteger addAsRHS(float f) {
		return this + f;
	}
	
	public MyInteger and(MyInteger i) {
		return new MyInteger(this.value & i.getValue());
	}
	
	public MyInteger and(int i) {
		return new MyInteger(this.value & i);
	}
	
	public MyInteger andAsRHS(int i) {
		return this & i;
	}

	public MyInteger div(MyInteger i) {
		return new MyInteger(this.value / i.getValue());
	}
	
	public MyInteger div(int i) {
		return new MyInteger(this.value / i);
	}
	
	public MyInteger divAsRHS(int i) {
		return new MyInteger(i / this.value);
	}
	
	public boolean eq(MyInteger i) {
		return this.value == i.getValue();
	}
	
	public boolean eq(int i) {
		return this.value == i;
	}
	
	public boolean eqAsRHS(int i) {
		return eq(i);
	}
	
	public boolean neq(MyInteger i) {
		return this.value != i.getValue();
	}
	
	public boolean neq(int i) {
		return this.value != i;
	}
	
	public boolean neqAsRHS(int i) {
		return neq(i);
	}
	
	public boolean gt(MyInteger i) {
		return this.value > i.getValue();
	}
	
	public boolean gt(int i) {
		return this.value > i;
	}
	
	public boolean gtAsRHS(int i) {
		return i > this.value;
	}
	
	public boolean gte(MyInteger i) {
		return this.value >= i.getValue();
	}
	
	public boolean gte(int i) {
		return this.value >= i;
	}
	
	public boolean gteAsRHS(int i) {
		return i >= this.value;
	}
	
	public boolean lt(MyInteger i) {
		return this.value < i.getValue();
	}
	
	public boolean lt(int i) {
		return this.value < i;
	}
	
	public boolean ltAsRHS(int i) {
		return i < this.value;
	}
	
	public boolean lte(MyInteger i) {
		return this.value <= i.getValue();
	}
	
	public boolean lte(int i) {
		return this.value <= i;
	}
	
	public boolean lteAsRHS(int i) {
		return i <= this.value;
	}
}
