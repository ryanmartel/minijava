class Arrays {

    public static void main(String[] args) {
        System.out.println(new A().foo(4));
    }
}

class A {
    int[] a;
    boolean b;

    public int foo(int i) {
        a = new int[4];
        a[1] = 2;
        a[2] = 3;
        a[0] = 1;
        return a.length;
    }

}
