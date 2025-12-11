package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.LinearGradient.Stop;
import ui11.platform.dom.DOMPeerBase;

public class DOMLinearGradientPeer extends DOMPeerBase<HTMLElement> {

    private final LinearGradient linearGradient;

    public DOMLinearGradientPeer(LinearGradient linearGradient) {
        this.linearGradient = linearGradient;
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected void update() {
        elem().getStyle().setProperty("background-image", asCSSImage());
    }

    @Override
    protected String asCSSImage() {
        StringBuilder b = new StringBuilder();
        b.append("linear-gradient(");
        b.append(linearGradient.angleDeg());
        b.append("deg");
        for (Stop stop : linearGradient.stops())
            b.append(", ").append(stop.color()).append(' ').append(stop.pos().toString());
        b.append(')');
        String s = b.toString();
        return s;
    }
}
