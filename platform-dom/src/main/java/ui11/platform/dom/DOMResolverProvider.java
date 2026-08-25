package ui11.platform.dom;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.ResolverRegistry.Priority;
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

public class DOMResolverProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        addDomResolvers(r);
        addCSSBackgroundResolvers(r);
    }

    private void addDomResolvers(ResolverRegistry r) {
        r.add(Priority.NATIVE, ColorFill.class, ColorFillPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Text.class, TextElementPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, RasterImageView.class, rasterImageView -> {
            throw new RuntimeException("TODO");
        }).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, LinearGradient.class, DOMLinearGradientPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, ConicGradient.class, DOMConicGradientPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Mask.class, DOMMaskPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Opacity.class, DOMOpacityPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, RoundedCorners.class, DOMRoundedCornersPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, WebContentFrame.class, WebContentFramePeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Empty.class, _ -> new EmptyElementPeer()).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, DOMElementWidget.class, DOMElementWrapperPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, SVGImageView.class, svg -> {
            if (svg.embeddedWidgets().isEmpty())
                return new DOMImageElement(svg.source().toURIString(), svg.isInteractive());
            else {
                // TODO ilyenkor isInteractive ignorálva van
                if (svg.source() instanceof ImageSource.InlineStringSource inlineStringSource)
                    return new DOMTemplatedSVGPeer(inlineStringSource.content(), svg.embeddedWidgets());
                else
                    throw new RuntimeException("TODO templated SVG with non-inline source: " + svg);
            }
        }).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, JPEGImageView.class,
                        jpg -> new DOMImageElement(jpg.source().toURIString(), false)).
                offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, HTMLElementHint.class, DOMWrapperElementPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Hyperlink.class, DOMHyperlinkPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Video.class, DOMVideoPeer::new).offers(DOMElementHolder.class);

        // INPUT
        r.add(Priority.NATIVE, ClickListener.class,
                        c -> new Provider<>(CumulatingPropList.class,
                                CumulatingPropList.ofOnClick(c.handler()), c.content())).
                offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, FocusListener.class,
                        f -> new Provider<>(CumulatingPropList.class,
                                CumulatingPropList.ofFocus(f), f.content())).
                offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, PointerRegion.class,
                        pr -> new Provider<>(CumulatingPropList.class,
                                CumulatingPropList.ofPointerRegion(pr), pr.content())).
                offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, PointerTransparent.class,
                        pt -> cssClass("Pt", pt.content())).
                offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, CloseRequestListener.class, DOMCloseRequestListenerPeer::new).
                offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, WithCursor.class,
                        c -> new Provider<>(CumulatingPropList.class,
                                CumulatingPropList.ofCursor(c.cursor()), c.content())).
                offers(DOMElementHolder.class);

        // LAYOUT
        r.add(Priority.NATIVE, Align.class, DOMAlignPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Box.class, DOMBoxPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Padding.class, DOMPaddingPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Grid.class, DOMGridPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, LinearLayout.class, DOMLinearLayoutPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Overlay.class, DOMOverlayLayoutPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Flow.class, DOMFlowLayoutPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, PassiveSize.class, DOMPassiveSizePeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, CSSClassTag.class, c -> new Provider<>(CumulatingPropList.class,
                CumulatingPropList.ofCSSClass(c.className()), c.content())).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, WrapWithCSSClassTag.class,
                w -> cssClass(w.className(), overlay(w.content()))).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Scrollable.class, DOMScrollablePeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Hidden.class,
                        h -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofHidden(), h.content())).
                offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Gone.class, gone -> new Hidden(empty())).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Cover.class, DOMCoverPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, PassiveHeight.class, DOMPassiveHeightPeer::new).offers(DOMElementHolder.class);
        // TODO ha itt önmagát adjuk vissza, azt detektálni kéne. most csak végtelen ciklusba kerülünk tőle.

        // FORMATTED TEXT
        r.add(Priority.NATIVE, OrderedList.class, DOMOrderedListPeer::new).offers(DOMElementHolder.class);

        // CONTROL
        r.add(Priority.NATIVE, PlainTextEditor.class, DOMEditableTextPeer::new).offers(DOMElementHolder.class);
        //r.addPeerIndependent(DOMPeerCreationRequest.class, Button.class, b -> new WidgetStateRequest<>(() -> new ButtonPeer(b, this), cf));
        r.add(Priority.NATIVE, CheckBox.class, DOMCheckBoxPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, RadioButton.class, DOMRadioButtonPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, ComboBox.class, ComboBoxPeer::new).offers(DOMElementHolder.class);
        r.add(Priority.NATIVE, Slider.class, SliderPeer::new).offers(DOMElementHolder.class);
        // TODO r.addPeerIndependent(DOMPeerCreationRequest.class, StylesheetRef.class, sr -> handleStylesheet(sr), sr));
        r.add(Priority.NATIVE, Tooltip.class,
                t -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofTooltipTag(t), t.content())).
                offers(DOMElementHolder.class);
    }

    private void addCSSBackgroundResolvers(ResolverRegistry r) {
        // CSS background
        r.add(Priority.NATIVE, SVGImageView.class, svg -> {
            if (svg.isInteractive())
                throw new RuntimeException("interactive SVGImageView inside CSSBackgroundImageContext");
            if (!svg.embeddedWidgets().isEmpty())
                throw new RuntimeException("embedded widgets in SVG inside CSSBackgroundImageContext");
            return new DOMCoverPeer.CSSBackgroundImage(svg.source().toURI());
        }).offers(DOMCoverPeer.CSSBackgroundImage.class);
        r.add(Priority.NATIVE, SVGImageView.class,
                jpg -> new DOMCoverPeer.CSSBackgroundImage(jpg.source().toURI())).
                offers(DOMCoverPeer.CSSBackgroundImage.class);
    }
}
