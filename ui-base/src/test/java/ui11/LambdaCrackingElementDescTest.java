/*
package ui11;

import ui11.observable.Observable;
import ui11.text.Text;
import ui11.lambda.LambdaCrackingElementDesc;
import ui11.layout.multichild.LinearLayout;
import ui11.window.Desktop;

public class LambdaCrackingElementDescTest extends Element {

    private final Observable<Integer> o1 = new Observable<>(1);

    private int counter;

    @Override
    protected Widget build() {
        LinearLayout col = LinearLayout.column();
        col.add(new Text("A" + o1.get()));
        for (int i = 0; i < 10; i++) {
            int i2 = i;
            col.add(LambdaCrackingElementDesc.unit(() -> new Text("B" + i2 + ": " + o1.get())));
            col.add(LambdaCrackingElementDesc.unit(() -> new Text("######" + i2 + ": " + counter++)));
        }
        return col;
    }

    public static void main(String[] args) throws InterruptedException {
        LambdaCrackingElementDescTest e = new LambdaCrackingElementDescTest();
        Desktop.getDesktop().openWindow(e);
        while (true) {
            Thread.sleep(1000);
            e.o1.set(e.o1.get() + 1);
        }
    }
}
*/