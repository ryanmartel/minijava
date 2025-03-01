class ExtendedScope {
    public static void main(String[] args) {
        System.out.println(new B().B_Method1());
    }
}

class A {
    int a1;
    int c1;

    public int A_method() {
        return 5;
    }
}

class B extends A{
    int b1;
    int c1;

    public int B_Method1() {
        a1 = 4;
        b1 = 8;
        return (a1 + b1);
    }
}
