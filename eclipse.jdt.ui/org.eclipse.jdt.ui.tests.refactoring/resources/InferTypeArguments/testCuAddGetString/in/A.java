package p;

import java.util.ArrayList;
import java.util.List;

class A {
	void foo() {
		List l= new ArrayList();
		l.add("Eclipse");
		String eclipse= (String) l.get(0);
	}
}