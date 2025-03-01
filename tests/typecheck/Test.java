class Test {
    public static void main(String[] args) {
        System.out.println(new A().foo());
    }
}

class A {
    int[] b;
    boolean c;

    public int foo() {
        c = false;
        b = new int[A.foo()];
        return 5;
    }
}
