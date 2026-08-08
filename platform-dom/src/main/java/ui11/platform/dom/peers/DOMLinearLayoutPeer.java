package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.ParentData;
import ui11.PeerRequestor;
import ui11.Widget;
import ui11.geom.Length;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.LinearLayout.JustifyContent;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMEnvironment;
import ui11.platform.dom.DOMLayoutPeerBase;

import java.util.*;

public class DOMLinearLayoutPeer extends DOMLayoutPeerBase {

    private static final String CLASS_FH = "Fh";
    private static final String CLASS_FV = "Fv";
    private static final String CLASS_Fa = "Fa";

    private final LinearLayout linearLayout;

    @Inject private DOMEnvironment env; // TODO ez így ronda

    @Remember private BitSet gone;
    @Remember private Map<Integer, HTMLElement> sizingWrappers; // TODO mem leak

    public DOMLinearLayoutPeer(LinearLayout linearLayout) {
        super(true, false);
        this.linearLayout = linearLayout;
    }

    @Override
    protected void initState() {
        gone = new BitSet();
        sizingWrappers = new HashMap<>();
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        return makePeers(linearLayout.items(), Set.of(LinearLayout.WeightMarker.WeightRequest.INSTANCE),
                (domElems, otherResults) -> doBuild2(
                        domElems,
                        (List<? extends LinearLayout.WeightMarker>) otherResults.get(
                                LinearLayout.WeightMarker.WeightRequest.INSTANCE)));
    }

    private Widget doBuild2(List<? extends DOMElementHolder> results,
                            List<? extends LinearLayout.WeightMarker> weightResults) {
        LinearLayout e = linearLayout;
        HTMLElement htmlElement = elem();
        List<HTMLElement> childElements = new ArrayList<>();
        gone.clear();
        for (int i = 0; i < results.size(); i++) {
            DOMElementHolder childH = results.get(i);
            if (childH.isHidden())
                gone.set(i); // TODO ez így hibás, mert hozzá kéne adni a DOM-hoz
            else if (childH.element().getNodeName().equalsIgnoreCase("img")) {
                // https://stackoverflow.com/questions/21103622/auto-resize-image-in-css-flexbox-layout-and-keeping-aspect-ratio
                // ChallengedPlayerSummary-ben a gender ikonnál jött elő, mert nem volt neki width/height megadva.
                // enélkül az img a konténer teljes szélességét próbálná kitölteni.
                // nem lehet ezt valahogy ilyen wrapper nélkül?
                // TODO ha be van állítva fixed height az img-n, akkor nem kell a sizingwrapper
                HTMLElement sizingWrapper = sizingWrappers.computeIfAbsent(i, _ -> {
                    HTMLElement div = env.document.createElement("div");
                    div.getClassList().add(DOMAlignPeer.CLASS_CHILDREN_MAX_SIZE_IS_100PERCENT);
                    return div;
                });
                if (sizingWrapper.getChildren().getLength() == 0 || sizingWrapper.getChildren().item(0) != childH.element()) {
                    sizingWrapper.setInnerHTML("");
                    sizingWrapper.appendChild(childH.element());
                }
                childElements.add(sizingWrapper);
            } else
                childElements.add(childH.element());
        }

        switch (e.mainAxis()) {
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
        for (int i = 0; i < results.size(); i++) {
            double weight = weightResults.get(i).weight();
            if (weight != 0) { // TODO ez most Gone-okat is figyelembe veszi?
                everyWeightIsZero = false;
                break;
            }
        }

        if (everyWeightIsZero && e.mainAxisAlignment() == JustifyContent.STRETCH)
            htmlElement.getClassList().add(CLASS_Fa);
        else
            htmlElement.getClassList().remove(CLASS_Fa);

        Length gap = e.gap();
        if (gap.isZero())
            htmlElement.getStyle().removeProperty("gap");
        else
            htmlElement.getStyle().setProperty("gap", lengthToCSS(gap));

        switch (e.crossAxisAlignment()) {
            case STRETCH -> htmlElement.getStyle().removeProperty("align-items");
            case START -> htmlElement.getStyle().setProperty("align-items", "start");
            case CENTER -> htmlElement.getStyle().setProperty("align-items", "center");
            case END -> htmlElement.getStyle().setProperty("align-items", "end");
            default -> throw new RuntimeException("unknown CAA: " + e.crossAxisAlignment());
        }

        switch (e.mainAxisAlignment()) {
            case STRETCH -> htmlElement.getStyle().removeProperty("justify-content");
            case START -> htmlElement.getStyle().setProperty("justify-content", "start");
            case CENTER -> htmlElement.getStyle().setProperty("justify-content", "center");
            case END -> htmlElement.getStyle().setProperty("justify-content", "end");
            case SPACE_EVENLY -> htmlElement.getStyle().setProperty("justify-content", "space-evenly");
            case SPACE_AROUND -> htmlElement.getStyle().setProperty("justify-content", "space-around");
            case SPACE_BETWEEN -> htmlElement.getStyle().setProperty("justify-content", "space-between");
            default -> throw new RuntimeException("unknown MAA: " + e.mainAxisAlignment());
        }

        /*
        Insets padding = e.padding.get();
        if (padding.isZero())
            htmlElement.getStyle().removeProperty("padding");
        else
            htmlElement.getStyle().setProperty("padding", insetsToCSS(padding));
         */

        int j = 0;
        for (int i = 0; i < results.size(); i++) {
            if (gone.get(i))
                continue;

            double weight = e.mainAxisAlignment() == JustifyContent.STRETCH ?
                    weightResults.get(i).weight() : 0;
            HTMLElement childPeer = childElements.get(j++);
            if (weight == 0)
                childPeer.getStyle().removeProperty("flex-grow");
            else
                childPeer.getStyle().setProperty("flex-grow", Double.toString(weight));
        }

        return updateChildren(childElements);
    }
}
