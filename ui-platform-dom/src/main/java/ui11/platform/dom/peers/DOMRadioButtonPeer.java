package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLInputElement;
import ui11.Widget;
import ui11.control.RadioButton;
import ui11.layout.singlechild.Align;
import ui11.observable.Observable;
import ui11.platform.dom.DOMElementWidget;
import ui11.platform.dom.DOMEnvironment;
import ui11.platform.dom.HTMLElementHint;

import java.util.Objects;

import static ui11.css.CSSClassTag.cssClass;
import static ui11.layout.multichild.LinearLayout.row;
import static ui11.layout.multichild.LinearLayout.withWeight;

public class DOMRadioButtonPeer<T> extends Widget {

    private final RadioButton<T> radioButton;

    @Inject private Observable<DOMEnvironment> env;

    @State private HTMLInputElement nativeCheckbox;

    public DOMRadioButtonPeer(RadioButton<T> radioButton) {
        this.radioButton = radioButton;
    }

    @Override
    protected void initState() {
        // TODO observable olvasása innen 
        nativeCheckbox = (HTMLInputElement) env.get().document.createElement("input");
        nativeCheckbox.setType("radio");
    }

    @Override
    protected Widget build() {
        // TODO ezt csak egyszer kéne
        untilNextRebuild().onClose(nativeCheckbox.onInput(evt -> {
            if (nativeCheckbox.isChecked())
                radioButton.prop().set(radioButton.value());
        })::dispose);

        nativeCheckbox.setChecked(Objects.equals(radioButton.prop().get(), radioButton.value()));
        nativeCheckbox.setDisabled(radioButton.disabled());

        if (radioButton.graphic() == null)
            return new DOMElementWidget(nativeCheckbox);
        else
            return new HTMLElementHint("label",
                    row(
                            new DOMElementWidget(nativeCheckbox),
                            withWeight(1, Align.leftCenter(
                                    cssClass("radiobutton-graphic", radioButton.graphic())
                            ))
                    )
            );
    }
}
