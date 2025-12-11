package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.graphics.fill.ConicGradient;
import ui11.graphics.fill.ConicGradient.Stop;
import ui11.platform.dom.DOMPeerBase;

public class DOMConicGradientPeer extends DOMPeerBase<HTMLElement> {

    private final ConicGradient conicGradient;

    public DOMConicGradientPeer(ConicGradient conicGradient) {
        this.conicGradient = conicGradient;
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
        double firstDeg = conicGradient.stops().getFirst().degrees();
        b.append("conic-gradient(from ").append(firstDeg).append("deg");
        for (Stop stop : conicGradient.stops()) {
            b.append(", ").append(stop.color()).append(' ').append(stop.degrees() - firstDeg).append("deg");
        }
        b.append(')');
        return b.toString();
    }
}
