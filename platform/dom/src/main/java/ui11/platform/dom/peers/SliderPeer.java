package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLInputElement;
import ui11.control.Slider;
import ui11.platform.dom.DOMPeerBase;

public class SliderPeer extends DOMPeerBase<HTMLInputElement> {

    private final Slider slider;

    public SliderPeer(Slider slider) {
        this.slider = slider;
    }

    @Override
    protected String elementName() {
        return "input";
    }

    @Override
    protected void initElement() {
        //htmlElement.getStyle().setProperty("transform", "rotate(180deg)");
        elem().setType("range");
        elem().setAttribute("min", "0");
        elem().setAttribute("max", "1");
        elem().setAttribute("step", "any");
    }

    @Override
    protected void update() {
        untilNextRebuild().onClose(elem().onInput(evt -> {
            slider.value().set(Double.parseDouble(elem().getValue()));
        })::dispose);
        elem().setValue(Double.toString(slider.value().get()));
    }
}
