package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.platform.dom.DOMPeerBase;
import ui11.text.Text;

public class TextElementPeer extends DOMPeerBase<HTMLElement> {

    /**
     * arra használja CSS, hogy display:inline-t beállítja ha flowban van a szöveg. illetve flowkra is ugyanezt
     * csinálja.
     */
    private static final String CLASS_TEXT = "Ta";

    private final Text text;

    public TextElementPeer(Text text) {
        this.text = text;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_TEXT);
    }

    @Override
    protected void update() {
        elem().setInnerText(text.text());
    }
}
