package operator_in;

public class TestTimesPlus {
	int result;
	
	public void foo() {
		result= /*]*/inline(20 / 10)/*[*/;
	}
	
	public int inline(int x) {
		return 1 / x;
	}
}