package ui11.input.focus;

import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.input.pointer.Pointer;
import ui11.input.pointer.Pointer.Button;
import ui11.input.pointer.PointerRegion;
import ui11.window.Window;

import org.jspecify.annotations.Nullable;

public class PointerRegionFindTest {
    public static void main(String[] args) {
        Window.open(new PointerRegion(new ColorFill(Color.GREEN)) {
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
