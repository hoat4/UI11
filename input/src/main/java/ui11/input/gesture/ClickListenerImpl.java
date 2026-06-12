package ui11.input.gesture;

import ui11.Widget;
import ui11.input.pointer.Pointer;
import ui11.input.pointer.Pointer.Button;
import ui11.input.pointer.PointerRegion;

public class ClickListenerImpl extends Widget {

    private final ClickListener clickListener;

    @Remember private boolean pressed;

    public ClickListenerImpl(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    protected Widget build() {
        return new PointerRegion(clickListener.content()) {

            @Override
            public PointerListener onPointerDown(Pointer pointer, Button button) {
                if (pressed)
                    return null;

                Button firstButton = button;
                pressed = true;
                return new PointerListener() {
                    private boolean fired;

                    @Override
                    public void onPress(Button button) {
                    }

                    @Override
                    public void onMove() {
                    }

                    @Override
                    public void onRelease(Button button) {
                        if (!fired && button.equals(firstButton)) {
                            clickListener.handler().run();
                            pressed = false;
                            fired = true;
                        }
                    }

                    @Override
                    public void onFinish() {
                        pressed = false;
                    }
                };
            }

            @Override
            public void onPointerMove(Pointer pointer, boolean inside) {
            }
        };
    }
}
