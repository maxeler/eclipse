package lambdaExpression18_in;

@FunctionalInterface
interface I1 {
	int foo(int a);
}

class X {
	class Y {
		I1 i1 = (int a) -> {
			/*[*/int b = 10;/*]*/
			return a + b;
		};
	}
}