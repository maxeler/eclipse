package object_out;

public class TestCompoundWrite2 {
	private String field;
	
	public void foo() {
		setField(getField() + "d" + "e");
	}

	String getField() {
		return field;
	}

	void setField(String field) {
		this.field = field;
	}
}
