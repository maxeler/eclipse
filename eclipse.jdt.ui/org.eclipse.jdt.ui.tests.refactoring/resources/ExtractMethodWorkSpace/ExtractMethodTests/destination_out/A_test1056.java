package destination_in;

public class A_test1056 {
	public class Inner {
		public void extracted() {

		}
		public class InnerMost {
			public int foo() {
				return A_test1056.this.extracted();
			}
		}
	}

	protected int extracted() {
		return /*[*/2 + 3/*]*/;
	}
	
}
