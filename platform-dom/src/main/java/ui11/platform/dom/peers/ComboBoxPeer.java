package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLOptionElement;
import org.teavm.jso.dom.html.HTMLSelectElement;
import ui11.Widget;
import ui11.control.ComboBox;
import ui11.platform.dom.DOMPeerBase;

import org.jspecify.annotations.NonNull;
import java.util.HashMap;
import java.util.Map;

public class ComboBoxPeer<T> extends DOMPeerBase<HTMLSelectElement> {

    private final ComboBox<T> comboBox;

    @Remember private Map<T, String> identifiers;

    public ComboBoxPeer(ComboBox<T> comboBox) {
        this.comboBox = comboBox;
    }

    @Override
    protected String elementName() {
        return "select";
    }

    @Override
    protected void initState() {
        identifiers = new HashMap<>();
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        // TODO fel kéne iratkozni a natív érték megváltozására
        elem().setValue(valID(comboBox.model().selectedValue.get()));

        elem().setInnerHTML("");
        for (T t : comboBox.model().possibleValues) {
            HTMLOptionElement option = (HTMLOptionElement) env().document.createElement("option");
            option.setValue(valID(t));
            String text = comboBox.displayNames().apply(t);
            if (text == null)
                // TODO mi legyen ilyenkor?
                text = "null";
            option.setInnerText(text);
            elem().appendChild(option);
        }

        return endingWidget();
    }

    private @NonNull String valID(T v) {
        return identifiers.computeIfAbsent(v, __ -> "v" + identifiers.size());
    }
}
