package ui11.platform.dom;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.*;
import ui11.control.*;
import ui11.css.CSSClassTag;
import ui11.css.WrapWithCSSClassTag;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.effect.Mask;
import ui11.graphics.effect.Opacity;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.fill.ConicGradient;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.RasterImageView;
import ui11.graphics.shaper.RoundedCorners;
import ui11.input.focus.FocusListener;
import ui11.input.gesture.ClickListener;
import ui11.input.gesture.CloseRequestListener;
import ui11.input.pointer.PointerRegion;
import ui11.input.pointer.PointerTransparent;
import ui11.input.pointer.WithCursor;
import ui11.layout.Gone;
import ui11.layout.multichild.Grid;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.flow.Flow;
import ui11.layout.singlechild.*;
import ui11.media.ImageSource;
import ui11.media.JPEGImageView;
import ui11.media.SVGImageView;
import ui11.media.Video;
import ui11.platform.dom.peers.*;
import ui11.provide.Provider;
import ui11.text.Text;
import ui11.text.formatted.OrderedList;
import ui11.webcontent.WebContentFrame;

import static ui11.css.CSSClassTag.cssClass;
import static ui11.graphics.Empty.empty;
import static ui11.graphics.effect.Overlay.overlay;

class DOMElementWidgetResolver extends WidgetResolver {

    @Override
    protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
        return switch (widget) {
            case ColorFill cf -> new ColorFillPeer(cf);
            case Text s -> new TextElementPeer(s);
            case RasterImageView iv -> {
                throw new RuntimeException("TODO");
            }
            case LinearGradient g -> new DOMLinearGradientPeer(g);
            case ConicGradient g -> new DOMConicGradientPeer(g);
            case Mask m -> new DOMMaskPeer(m);
            case Opacity m -> new DOMOpacityPeer(m);
            case RoundedCorners r -> new DOMRoundedCornersPeer(r);
            case WebContentFrame wcf -> new WebContentFramePeer(wcf);
            case Empty e -> new EmptyElementPeer();
            case DOMElementWidget e -> new DOMElementWrapperPeer(e);
            case SVGImageView svg -> {
                if (svg.embeddedWidgets().isEmpty())
                    yield new DOMImageElement(svg.source().toURIString(), svg.isInteractive());
                else {
                    // TODO ilyenkor isInteractive ignorálva van
                    if (svg.source() instanceof ImageSource.InlineStringSource inlineStringSource)
                        yield new DOMTemplatedSVGPeer(inlineStringSource.content(), svg.embeddedWidgets());
                    else
                        throw new RuntimeException("TODO templated SVG with non-inline source: " + svg);
                }
            }
            case JPEGImageView jpg -> new DOMImageElement(jpg.source().toURIString(), false);
            case HTMLElementHint h -> new DOMWrapperElementPeer(h);
            case Hyperlink l -> new DOMHyperlinkPeer(l);
            case Video video -> new DOMVideoPeer(video);

            // INPUT
            case ClickListener c -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofOnClick(c.handler()), c.content());
            case FocusListener f -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofFocus(f), f.content());
            case PointerRegion r -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofPointerRegion(r), r.content());
            case PointerTransparent pt -> cssClass("Pt", pt.content());
            case CloseRequestListener closeRequestListener -> new DOMCloseRequestListenerPeer(closeRequestListener);
            case WithCursor c -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofCursor(c.cursor()), c.content());

            // LAYOUT
            case Align a -> new DOMAlignPeer(a);
            case Box b -> new DOMBoxPeer(b);
            case Padding b -> new DOMPaddingPeer(b);
            case Grid g -> new DOMGridPeer(g);
            case LinearLayout l -> new DOMLinearLayoutPeer(l);
            case Overlay o -> new DOMOverlayLayoutPeer(o);
            case Flow f -> new DOMFlowLayoutPeer(f);
            case PassiveSize p -> new DOMPassiveSizePeer(p);
            case CSSClassTag c -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofCSSClass(c.className()), c.content());
            case WrapWithCSSClassTag w -> cssClass(w.className(), overlay(w.content()));
            case Scrollable s -> new DOMScrollablePeer(s);
            case Hidden h -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofHidden(), h.content());
            case Gone gone -> new Hidden(empty());
            case Cover c -> new DOMCoverPeer(c);
            case PassiveHeight p -> new DOMPassiveHeightPeer(p);
            // TODO ha itt önmagát adjuk vissza, azt detektálni kéne. most csak végtelen ciklusba kerülünk tőle.

            // FORMATTED TEXT
            case OrderedList ol -> new DOMOrderedListPeer(ol);

            // CONTROL
            case PlainTextEditor et -> new DOMEditableTextPeer(et);
            //case Button b -> new WidgetStateRequest<>(() -> new ButtonPeer(b, this), cf);
            case CheckBox cb -> new DOMCheckBoxPeer(cb);
            case RadioButton<?> rb -> new DOMRadioButtonPeer<>(rb);
            case ComboBox<?> cb -> new ComboBoxPeer<>(cb);
            case Slider slider -> new SliderPeer(slider);
            // TODO case StylesheetRef sr -> handleStylesheet(sr), sr);
            case Tooltip t -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofTooltipTag(t), t.content());

            default -> null;
        };
    }

    @Override
    protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget,
                                                         @NonNull PeerRequest<?> request) {
        return null;
    }
}
