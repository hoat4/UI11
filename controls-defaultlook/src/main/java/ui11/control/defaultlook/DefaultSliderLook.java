package ui11.control.defaultlook;

import ui11.Widget;
import ui11.control.Slider;
import ui11.text.Text;

// TODO
public final class DefaultSliderLook extends Widget {

    private final Slider slider;

    public DefaultSliderLook(Slider slider) {
        this.slider = slider;
    }

    @Override
    protected Widget build() {
        return new Text("--------|---------");
    }
}
