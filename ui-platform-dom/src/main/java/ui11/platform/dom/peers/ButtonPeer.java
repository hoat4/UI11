package ui11.platform.dom.peers;

/*
public class ButtonPeer extends DOMPeer<Button, HTMLButtonElement> {

    public ButtonPeer(Button button, DOMEnvironment env) {
        super(button, env);
    }

    @Override
    protected HTMLButtonElement createElement() {
        return (HTMLButtonElement) env.document().createElement("button");
    }

    @RepeatedInit
    private void updateContent() {
        htmlElement.innerHTML("");
        htmlElement.appendChild(makeChildHtmlElement(e.content));
    }

    @Override
    public Rect boundsOfChild(Node element) {
        assert e.content == element;
        return shape().bounds();
    }

    @Override
    protected Sizing sizing() {
        return Node.sizingTmp(e.content);
    }
}
*/