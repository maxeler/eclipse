package p;
//use Object
class A{
	public void foo(){};
}
class Test{
	void test() throws CloneNotSupportedException, InterruptedException{
		A a= new A();
		a.getClass();
		a.equals(null);
		a.hashCode();
		a.notify();
		a.notifyAll();
		a.toString();
		a.wait();
		a.wait(0);
		a.wait(0, 0);
	}
}