package ui11.observable;

public class DerivedValueTest {

    // TODO azt is kéne tesztelni hogy get után mikor lesznek invalidálva az observerek

    public static void main(String[] args) {
        ObservableImpl<Integer> a = new ObservableImpl<>(10);
        ObservableImpl<Integer> b = new ObservableImpl<>(20);

        Observable<Integer> d = Observable.of(() -> {
            int aVal = a.get(), bVal = b.get();
            System.out.println(aVal + " + " + bVal + " = " + (aVal + bVal));
            return aVal + bVal;
        });

        System.out.println("created");
        System.out.println("value (1): " + d.get());
        System.out.println("value (2): " + d.get());
        System.out.println("a := 11");
        a.set(11);
        System.out.println("written");
        System.out.println("b := 21");
        a.set(21);
        System.out.println("written");
        System.out.println("value: " + d.get());

        System.out.println("subscribe");

        SimpleScope scope = new SimpleScope(Scope.global());
        d.changes().subscribe(scope, System.out::println);

        System.out.println("a := 14");
        a.set(14);
        System.out.println("b := 24");
        b.set(24);
        System.out.println("read");
        System.out.println("value: " + d.get());
        System.out.println("value: " + d.get());
        System.out.println("a := 15");
        a.set(15);
        System.out.println("b := 26");
        a.set(26);
        System.out.println("value: " + d.get());

        scope.close();
        System.out.println("unsubscribed");
        System.out.println("a := 11");
        a.set(11);
        System.out.println("written");
        System.out.println("value: " + d.get());
        System.out.println("value: " + d.get());
    }
}
