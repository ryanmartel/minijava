class InheritedMethod {
    public static void main(String[] args) {
        B b;
        b = new B();
        System.out.println(b.A_Method());
    }
}

class A {
    public int A_Method() {
        int a1;
        a1 = 5;
        return a1;
    }
}

class B extends A {
    public int B_Method() {
        int b1;
        b1 = 4;
        return b1;
    }
}
