class SimpleScope {
    public static void main (String[] args) {
        System.out.println(new B().B_Method(5));
    }
}

class A {
    int a1;

    public int A_Method() {
        int a2;
        a1 = 4;
        a2 = 8;
        return a1 + a2;
    }
}

class B {
    int b1;
    A a;

    public int B_Method(int b2) {
        a = new A();
        return (b2 + (a.A_Method()));
    }
}
