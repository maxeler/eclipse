package p;

class A {
	int x;

	protected void m() { 
		class T extends A{
			void t(){
				super.x++;
			}
		};
	}
}

class B extends A {
}
