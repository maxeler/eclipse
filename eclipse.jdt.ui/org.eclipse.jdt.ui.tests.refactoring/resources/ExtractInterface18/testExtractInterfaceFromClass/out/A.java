package p;

public class A implements B {	

	/* (non-Javadoc)
	 * @see p.B#m1(java.lang.String)
	 */
	public void m1(String s) {
		System.out.println(s);
	}

	public static void statictM1(String s) {
		System.out.println(s);
	}
}