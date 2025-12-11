package ui11.input.pointer;

import ui11.Widget;
import ui11.input.pointer.Pointer.Button;

public class IsPressedListenerImpl extends Widget {

    private final IsPressedListener isPressedListener;

    @State private boolean pressed;

    public IsPressedListenerImpl(IsPressedListener isPressedListener) {
        this.isPressedListener = isPressedListener;
    }

    @Override
    protected void initState() {
    }

    @Override
    protected Widget build() {
        isPressedListener.consumer().accept(pressed); // setPressedet lehet hogy observable-k nélkül kéne meghívni

        return new PointerRegion(isPressedListener.content()) {

            @Override
            public PointerListener onPointerDown(Pointer pointer, Button button) {
                if (!pressed) {
                    pressed = true;
                    isPressedListener.consumer().accept(true);
                    return new PointerListener() {
                        @Override
                        public void onPress(Button button) {
                        }

                        @Override
                        public void onMove() {
                        }

                        @Override
                        public void onRelease(Button button) {
                        }

                        @Override
                        public void onFinish() {
                            pressed = false;
                            isPressedListener.consumer().accept(false);
                        }
                    };
                } else
                    return null;
            }

            @Override
            public void onPointerMove(Pointer pointer, boolean inside) {
            }
        };
    }
}
