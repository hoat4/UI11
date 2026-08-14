package ui11.platform.dom;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
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
import ui11.platform.dom.DOMPeerBase.DOMPeerCreationRequest;
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
        r.addPeerIndependent(DOMPeerCreationRequest.class, ColorFill.class, ColorFillPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Text.class, TextElementPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, RasterImageView.class, rasterImageView -> {
            throw new RuntimeException("TODO");
        });
        r.addPeerIndependent(DOMPeerCreationRequest.class, LinearGradient.class, DOMLinearGradientPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, ConicGradient.class, DOMConicGradientPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Mask.class, DOMMaskPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Opacity.class, DOMOpacityPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, RoundedCorners.class, DOMRoundedCornersPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, WebContentFrame.class, WebContentFramePeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Empty.class, _ -> new EmptyElementPeer());
        r.addPeerIndependent(DOMPeerCreationRequest.class, DOMElementWidget.class, DOMElementWrapperPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, SVGImageView.class, svg -> {
            if (svg.embeddedWidgets().isEmpty())
                return new DOMImageElement(svg.source().toURIString(), svg.isInteractive());
            else {
                // TODO ilyenkor isInteractive ignorálva van
                if (svg.source() instanceof ImageSource.InlineStringSource inlineStringSource)
                    return new DOMTemplatedSVGPeer(inlineStringSource.content(), svg.embeddedWidgets());
                else
                    throw new RuntimeException("TODO templated SVG with non-inline source: " + svg);
            }
        });
        r.addPeerIndependent(DOMPeerCreationRequest.class, JPEGImageView.class,
                jpg -> new DOMImageElement(jpg.source().toURIString(), false));
        r.addPeerIndependent(DOMPeerCreationRequest.class, HTMLElementHint.class, DOMWrapperElementPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Hyperlink.class, DOMHyperlinkPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Video.class, DOMVideoPeer::new);

        // INPUT
        r.addPeerIndependent(DOMPeerCreationRequest.class, ClickListener.class,
                c -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofOnClick(c.handler()), c.content()));
        r.addPeerIndependent(DOMPeerCreationRequest.class, FocusListener.class,
                f -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofFocus(f), f.content()));
        r.addPeerIndependent(DOMPeerCreationRequest.class, PointerRegion.class,
                pr -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofPointerRegion(pr), pr.content()));
        r.addPeerIndependent(DOMPeerCreationRequest.class, PointerTransparent.class,
                pt -> cssClass("Pt", pt.content()));
        r.addPeerIndependent(DOMPeerCreationRequest.class, CloseRequestListener.class, DOMCloseRequestListenerPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, WithCursor.class,
                c -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofCursor(c.cursor()), c.content()));

        // LAYOUT
        r.addPeerIndependent(DOMPeerCreationRequest.class, Align.class, DOMAlignPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Box.class, DOMBoxPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Padding.class, DOMPaddingPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Grid.class, DOMGridPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, LinearLayout.class, DOMLinearLayoutPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Overlay.class, DOMOverlayLayoutPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Flow.class, DOMFlowLayoutPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, PassiveSize.class, DOMPassiveSizePeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, CSSClassTag.class, c -> new Provider<>(CumulatingPropList.class,
                CumulatingPropList.ofCSSClass(c.className()), c.content()));
        r.addPeerIndependent(DOMPeerCreationRequest.class, WrapWithCSSClassTag.class, w -> cssClass(w.className(), overlay(w.content())));
        r.addPeerIndependent(DOMPeerCreationRequest.class, Scrollable.class, DOMScrollablePeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Hidden.class, h -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofHidden(), h.content()));
        r.addPeerIndependent(DOMPeerCreationRequest.class, Gone.class, gone -> new Hidden(empty()));
        r.addPeerIndependent(DOMPeerCreationRequest.class, Cover.class, DOMCoverPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, PassiveHeight.class, DOMPassiveHeightPeer::new);
        // TODO ha itt önmagát adjuk vissza, azt detektálni kéne. most csak végtelen ciklusba kerülünk tőle.

        // FORMATTED TEXT
        r.addPeerIndependent(DOMPeerCreationRequest.class, OrderedList.class, DOMOrderedListPeer::new);

        // CONTROL
        r.addPeerIndependent(DOMPeerCreationRequest.class, PlainTextEditor.class, DOMEditableTextPeer::new);
        //r.addPeerIndependent(DOMPeerCreationRequest.class, Button.class, b -> new WidgetStateRequest<>(() -> new ButtonPeer(b, this), cf));
        r.addPeerIndependent(DOMPeerCreationRequest.class, CheckBox.class, DOMCheckBoxPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, RadioButton.class, DOMRadioButtonPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, ComboBox.class, ComboBoxPeer::new);
        r.addPeerIndependent(DOMPeerCreationRequest.class, Slider.class, SliderPeer::new);
        // TODO r.addPeerIndependent(DOMPeerCreationRequest.class, StylesheetRef.class, sr -> handleStylesheet(sr), sr));
        r.addPeerIndependent(DOMPeerCreationRequest.class, Tooltip.class, t -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofTooltipTag(t), t.content()));
    }

    private void addCSSBackgroundResolvers(ResolverRegistry r) {
        // CSS background
        r.addPeerIndependent(DOMPeerBase.CSSBackgroundImagePeerCreationRequest.class, SVGImageView.class, svg -> {
            if (svg.isInteractive())
                throw new RuntimeException("interactive SVGImageView inside CSSBackgroundImageContext");
            if (!svg.embeddedWidgets().isEmpty())
                throw new RuntimeException("embedded widgets in SVG inside CSSBackgroundImageContext");
            return new DOMCoverPeer.CSSBackgroundImage(svg.source().toURI());
        });
        r.addPeerIndependent(DOMPeerBase.CSSBackgroundImagePeerCreationRequest.class, SVGImageView.class,
                jpg -> new DOMCoverPeer.CSSBackgroundImage(jpg.source().toURI()));
    }
}
