package ui11.input.focus;

import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.input.pointer.Pointer;
import ui11.input.pointer.Pointer.Button;
import ui11.input.pointer.PointerRegion;
import ui11.window.Desktop;

import javax.annotation.Nullable;

public class PointerRegionFindTest {
    public static void main(String[] args) {
        Desktop.getDesktop().openWindow(new PointerRegion(new ColorFill(Color.GREEN)) {
            @Nullable
            @Override
            public PointerListener onPointerDown(Pointer pointer, Button button) {
                System.out.println("DOWN");
                return null;
            }

            @Override
            public void onPointerMove(Pointer pointer, boolean inside) {
                System.out.println("MOVE");
            }
        });
    }
}
