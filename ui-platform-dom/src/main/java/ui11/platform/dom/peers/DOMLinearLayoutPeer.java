package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.MultiSlot;
import ui11.Widget;
import ui11.WidgetInstantiation;
import ui11.geom.Length;
import ui11.layout.Gone;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.LinearLayout.Item;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMWidgetWrapper;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class DOMLinearLayoutPeer extends DOMLayoutPeerBase {

    private static final String CLASS_FH = "Fh";
    private static final String CLASS_FV = "Fv";
    private static final String CLASS_Fa = "Fa";

    private final LinearLayout linearLayout;

    @Inject private MultiSlot<Integer> slots;

    @State private BitSet gone;

    public DOMLinearLayoutPeer(LinearLayout linearLayout) {
        super(true, false);
        this.linearLayout = linearLayout;
    }

    @Override
    protected void initState() {
        gone = new BitSet();
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected List<? extends HTMLElement> children() {
        LinearLayout e = linearLayout;
        HTMLElement htmlElement = elem();

        List<HTMLElement> childElements = new ArrayList<>();
        gone.clear();
        List<? extends Widget> items = e.items();
        for (int i = 0; i < items.size(); i++) {
            Widget o = items.get(i);
            WidgetInstantiation childH = slots.instantiate(i, new DOMWidgetWrapper(o));
            if (childH.lookupOptional(Gone.class).isPresent())
                gone.set(i);
            else
                childElements.add(childH.lookup(DOMElementHolder.class).element());
        }

        switch (e.axis()) {
            case VERTICAL -> {
                htmlElement.getClassList().remove(CLASS_FH);
                htmlElement.getClassList().add(CLASS_FV);
            }
            case HORIZONTAL -> {
                htmlElement.getClassList().remove(CLASS_FV);
                htmlElement.getClassList().add(CLASS_FH);
            }
            default -> throw new RuntimeException("should not reach here (LL)");
        }

        boolean everyWeightIsZero = true;
        for (Widget item : e.items()) {
            if (Item.weight(item) != 0) { // TODO ez most Gone-okat is figyelembe veszi
                everyWeightIsZero = false;
                break;
            }
        }

        if (everyWeightIsZero)
            htmlElement.getClassList().add(CLASS_Fa);
        else
            htmlElement.getClassList().remove(CLASS_Fa);

        Length gap = e.gap();
        if (gap.isZero())
            htmlElement.getStyle().removeProperty("gap");
        else
            htmlElement.getStyle().setProperty("gap", lengthToCSS(gap));

        /*
        Insets padding = e.padding.get();
        if (padding.isZero())
            htmlElement.getStyle().removeProperty("padding");
        else
            htmlElement.getStyle().setProperty("padding", insetsToCSS(padding));
         */

        int j = 0;
        for (int i = 0; i < e.items().size(); i++) {
            if (gone.get(i))
                continue;

            Widget item = e.items().get(i);

            double weight = Item.weight(item);
            HTMLElement childPeer = childElements.get(j++);
            if (weight == 0)
                childPeer.getStyle().removeProperty("flex-grow");
            else
                childPeer.getStyle().setProperty("flex-grow", Double.toString(weight));
        }

        return childElements;
    }
}
