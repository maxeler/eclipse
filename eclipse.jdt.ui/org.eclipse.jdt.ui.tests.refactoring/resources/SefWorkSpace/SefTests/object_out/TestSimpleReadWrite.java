package object_out;

public class TestSimpleReadWrite {
	private String field;
	
	public void foo() {
		setField(getField() + "field");
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}
}

