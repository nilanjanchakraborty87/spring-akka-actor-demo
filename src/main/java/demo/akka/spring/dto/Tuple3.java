package demo.akka.spring.dto;

public class Tuple3<A, B, C> {

    public final A a;
    public final B b;
    public final C c;

    private Tuple3(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> Tuple3 of (A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }

}
