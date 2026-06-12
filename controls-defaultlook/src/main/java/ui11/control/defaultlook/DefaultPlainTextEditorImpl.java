package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.EditablePlainText;
import ui11.control.PlainTextEditor;
import ui11.geom.Location;
import ui11.input.focus.FocusHolder;
import ui11.input.focus.FocusRoot;
import ui11.input.gesture.EnterContentListener;
import ui11.input.gesture.EnterContentListener.EnterContent;
import ui11.input.keyboard.KeyCombination;
import ui11.input.keyboard.KeySymbol.StandardFunctionSymbol;
import ui11.input.keyboard.KeyTypeListener;
import ui11.input.pointer.MouseRegion;
import ui11.input.pointer.MouseRegion.MouseListener;
import ui11.input.pointer.Pointer.StandardMouseButton;
import ui11.observable.Observable;
import ui11.text.Text;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class DefaultPlainTextEditorImpl extends Widget {

    private final PlainTextEditor plainTextEditor;

    @Inject private FocusRoot focusRoot;

    @Remember private FocusHolder defaultFocusHolder;
    @Remember private FocusHolder focusHolder;

    public DefaultPlainTextEditorImpl(PlainTextEditor plainTextEditor) {
        this.plainTextEditor = plainTextEditor;
    }

    @Override
    protected Widget build() {
        if (plainTextEditor.focusHolder() == null) {
            if (defaultFocusHolder == null)
                defaultFocusHolder = new FocusHolder();
            focusHolder = defaultFocusHolder;
        } else
            focusHolder = plainTextEditor.focusHolder();

        return new KeyTypeListener(
                this::keyTyped,
                new EnterContentListener(
                        this::textEntered,
                        focusHolder,
                        new MouseRegion(
                                new Text(text().get()),
                                StandardMouseButton.PRIMARY, // TODO
                                new MouseListenerImpl()
                        )
                )
        );
    }

    private void textEntered(EnterContent enterContent) {
        try {
            Object o = enterContent.transferable().getTransferData(DataFlavor.stringFlavor);
            if (o instanceof String s)
                text().set(text().get() + s.replaceAll("[\r\n]", ""));
        } catch (UnsupportedFlavorException | IOException e) {
            throw new RuntimeException(e); // TODO;
        }
    }

    private void keyTyped(KeyCombination keyCombination) {
        if (keyCombination.keySymbol() == StandardFunctionSymbol.BACKSPACE) {
            String s = text().get();
            if (!s.isEmpty())
                text().set(s.substring(0, s.length() - 1));
        }
        if (keyCombination.keySymbol() == StandardFunctionSymbol.ENTER && plainTextEditor.onAction() != null)
            plainTextEditor.onAction().run();
    }

    private EditablePlainText text() {
        return plainTextEditor.editablePlainText();
    }

    private class MouseListenerImpl implements MouseListener {
        @Override
        public void hoverMoved(Location location) {
            // empty
        }

        @Override
        public void hoverMovedOut() {
            // empty
        }

        @Override
        public void down(Location location) {
            focusRoot.requestFocus(focusHolder);
        }

        @Override
        public void drag(Location location) {
            // empty
        }

        @Override
        public void up() {
            // empty
        }

        @Override
        public void cancel() {
            // empty
        }
    }
}