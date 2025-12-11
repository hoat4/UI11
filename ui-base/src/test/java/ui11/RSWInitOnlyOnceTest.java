package ui11;

import ui11.text.Text;
import ui11.window.Desktop;

// akkor sikeres, ha a 23-mas szám kiírása után 2 másodperccel megjelenik a 43,
// és a konzolon csak 1 db "initState" felirat van

public class RSWInitOnlyOnceTest extends Widget {

    private final int i;

    public RSWInitOnlyOnceTest(int i) {
        this.i = i;
    }

    @Override
    protected void initState() {
        System.out.println("initState");
    }

    @Override
    protected void onResume() {
        System.out.println("onResume");
    }

    @Override
    protected Widget build() {
        System.out.println("build (" + i + ")");
        return new Text(i);
    }

    public static void main(String[] args) throws InterruptedException {
        SlotOld slot = new SlotOld();
        slot.set(new RSWInitOnlyOnceTest(23));
        Desktop.getDesktop().openWindow(slot);
        Thread.sleep(2000);
        slot.set(new RSWInitOnlyOnceTest(43));
    }
}
