//renaming I.m to k
package p;
class B implements I{
	public void m(){};
} 
class A extends B implements I{
	public void m(){};
}
interface I {
	void m();
}