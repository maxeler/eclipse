package argument_in;

public class TestParameterNameUnused2 {
	public void main() {
		for (int x= 10; x < 20; x++)
			main();
		/*]*/foo(10);/*[*/
	}
	
	public void foo(int x) {
		x= 20;
		bar(x);
	}
	
	public void bar(int z) {
	}
}
