package ui11;

import ui11.color.Color;
import ui11.provide.Provider;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.decoration.Background.withBackground;
import static ui11.layout.multichild.LinearLayout.column;

public class TestUseStateRoleWidget extends Widget {

    static void main() {
        Window.open(new Provider<>(IntermediateWidget.Mode.class, IntermediateWidget.Mode.A,
                new TestUseStateRoleWidget()));
    }

    // we can't have an @Inject field here because then the freshly cloned widget's @Inject field
    // would have non-zero value which is illegal, so we have to go through an intermediate widget

    @Override
    protected Widget build() {
        return column(
                new Text("w = " + this),
                new IntermediateWidget(this)
        );
    }

    @Override
    public String toString() {
        return "W" + System.identityHashCode(this);
    }

    private static class IntermediateWidget extends Widget {

        @Inject private Mode mode;

        private final TestUseStateRoleWidget w; // this will be a state role instance

        public IntermediateWidget(TestUseStateRoleWidget w) {
            this.w = w;
        }

        @Override
        protected Widget build() {
            return switch (mode) {
                case A -> withBackground(Color.CYAN, new Provider<>(Mode.class, Mode.B, w));
                case B -> new Text("Hello world!");
            };
        }

        private enum Mode {
            A, B
        }
    }
}
