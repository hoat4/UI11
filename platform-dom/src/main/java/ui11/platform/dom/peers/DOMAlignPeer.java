package ui11.platform.dom.peers;

import ui11.Widget;
import ui11.css.WrapWithCSSClassTag;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;

import java.util.List;
import java.util.Objects;

public class DOMAlignPeer extends DOMLayoutPeerBase {

    private static final String[][] CLASS_ATTRS;
    private static final String[] CLASS_FILL = new String[]{"Fh", "FG"};

    // ez maradjon szinkronban HTMLAlignPeerrel
    static {
        CLASS_ATTRS = new String[Alignment.values().length][];
        CLASS_ATTRS[Alignment.TOP.ordinal()] = new String[]{"Fh", "Fs", "FG"};
        CLASS_ATTRS[Alignment.RIGHT.ordinal()] = new String[]{"Fh", "fe"};
        CLASS_ATTRS[Alignment.BOTTOM.ordinal()] = new String[]{"Fh", "Fe", "FG"};
        CLASS_ATTRS[Alignment.LEFT.ordinal()] = new String[]{"Fh", "fs"};
        CLASS_ATTRS[Alignment.HCENTER.ordinal()] = new String[]{"Fh", "fc"};
        CLASS_ATTRS[Alignment.VCENTER.ordinal()] = new String[]{"Fh", "Fc", "FG"};
        CLASS_ATTRS[Alignment.LEFT_TOP.ordinal()] = new String[]{"Fh", "fs", "Fs"};
        CLASS_ATTRS[Alignment.CENTER_TOP.ordinal()] = new String[]{"Fh", "fc", "Fs"};
        CLASS_ATTRS[Alignment.RIGHT_TOP.ordinal()] = new String[]{"Fh", "fe", "Fs"};
        CLASS_ATTRS[Alignment.LEFT_CENTER.ordinal()] = new String[]{"Fh", "fs", "Fc"};
        CLASS_ATTRS[Alignment.CENTER.ordinal()] = new String[]{"Fh", "fc", "Fc"};
        CLASS_ATTRS[Alignment.RIGHT_CENTER.ordinal()] = new String[]{"Fh", "fe", "Fc"};
        CLASS_ATTRS[Alignment.LEFT_BOTTOM.ordinal()] = new String[]{"Fh", "fs", "Fe"};
        CLASS_ATTRS[Alignment.CENTER_BOTTOM.ordinal()] = new String[]{"Fh", "fc", "Fe"};
        CLASS_ATTRS[Alignment.RIGHT_BOTTOM.ordinal()] = new String[]{"Fh", "fe", "Fe"};
    }

    static final String CLASS_CHILDREN_MAX_SIZE_IS_100PERCENT = "sm";
    private static final String CLASS_EXPAND_OUTSIDE = "xo";

    private final Align align;

    @Remember private String[] prev;

    public DOMAlignPeer(Align align) {
        super(false, false);
        this.align = align;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMLayoutPeerBase.CLASS_POINTER_TRANSPARENT_CONTAINER);
    }

    @Override
    protected Widget doBuild() {
        Widget alignedContent = align.content();
        Widget alignedContentWithCSSClass = align.allowExpandOutside() ?
                WrapWithCSSClassTag.wrapWithCssClass(CLASS_EXPAND_OUTSIDE, alignedContent) :
                alignedContent;

        return makePeer(alignedContentWithCSSClass, (DOMElementHolder child) -> {
            if (prev != null)
                elem().getClassList().remove(prev);
            String[] classes = align.alignment() == null ?
                    CLASS_FILL : CLASS_ATTRS[align.alignment().ordinal()];
            Objects.requireNonNull(classes);
            elem().getClassList().add(prev = classes);

            // TODO ez valszeg hülyén működik, ha allowExpandOutside == true és van insets

            if (align.allowExpandOutside()) {
                elem().getClassList().remove(CLASS_CHILDREN_MAX_SIZE_IS_100PERCENT);
            } else {
                // Gridnél most ilyesmi valszeg miatt kellett a a minmax(0, ...) hack
                elem().getClassList().add(CLASS_CHILDREN_MAX_SIZE_IS_100PERCENT);
            }

            elem().setAttribute("data-align",
                    align.alignment() == null ? "FILL" : align.alignment().displayName);

            return updateChildren(List.of(child.element()));
        });
    }
}
