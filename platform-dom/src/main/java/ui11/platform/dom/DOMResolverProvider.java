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
        r.registerForContextType(DOMPeerCreationRequest.class, ColorFill.class, ColorFillPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Text.class, TextElementPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, RasterImageView.class, rasterImageView -> {
            throw new RuntimeException("TODO");
        });
        r.registerForContextType(DOMPeerCreationRequest.class, LinearGradient.class, DOMLinearGradientPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, ConicGradient.class, DOMConicGradientPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Mask.class, DOMMaskPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Opacity.class, DOMOpacityPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, RoundedCorners.class, DOMRoundedCornersPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, WebContentFrame.class, WebContentFramePeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Empty.class, _ -> new EmptyElementPeer());
        r.registerForContextType(DOMPeerCreationRequest.class, DOMElementWidget.class, DOMElementWrapperPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, SVGImageView.class, svg -> {
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
        r.registerForContextType(DOMPeerCreationRequest.class, JPEGImageView.class,
                jpg -> new DOMImageElement(jpg.source().toURIString(), false));
        r.registerForContextType(DOMPeerCreationRequest.class, HTMLElementHint.class, DOMWrapperElementPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Hyperlink.class, DOMHyperlinkPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Video.class, DOMVideoPeer::new);

        // INPUT
        r.registerForContextType(DOMPeerCreationRequest.class, ClickListener.class,
                c -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofOnClick(c.handler()), c.content()));
        r.registerForContextType(DOMPeerCreationRequest.class, FocusListener.class,
                f -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofFocus(f), f.content()));
        r.registerForContextType(DOMPeerCreationRequest.class, PointerRegion.class,
                pr -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofPointerRegion(pr), pr.content()));
        r.registerForContextType(DOMPeerCreationRequest.class, PointerTransparent.class,
                pt -> cssClass("Pt", pt.content()));
        r.registerForContextType(DOMPeerCreationRequest.class, CloseRequestListener.class, DOMCloseRequestListenerPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, WithCursor.class,
                c -> new Provider<>(CumulatingPropList.class,
                        CumulatingPropList.ofCursor(c.cursor()), c.content()));

        // LAYOUT
        r.registerForContextType(DOMPeerCreationRequest.class, Align.class, DOMAlignPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Box.class, DOMBoxPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Padding.class, DOMPaddingPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Grid.class, DOMGridPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, LinearLayout.class, DOMLinearLayoutPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Overlay.class, DOMOverlayLayoutPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Flow.class, DOMFlowLayoutPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, PassiveSize.class, DOMPassiveSizePeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, CSSClassTag.class, c -> new Provider<>(CumulatingPropList.class,
                CumulatingPropList.ofCSSClass(c.className()), c.content()));
        r.registerForContextType(DOMPeerCreationRequest.class, WrapWithCSSClassTag.class, w -> cssClass(w.className(), overlay(w.content())));
        r.registerForContextType(DOMPeerCreationRequest.class, Scrollable.class, DOMScrollablePeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Hidden.class, h -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofHidden(), h.content()));
        r.registerForContextType(DOMPeerCreationRequest.class, Gone.class, gone -> new Hidden(empty()));
        r.registerForContextType(DOMPeerCreationRequest.class, Cover.class, DOMCoverPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, PassiveHeight.class, DOMPassiveHeightPeer::new);
        // TODO ha itt önmagát adjuk vissza, azt detektálni kéne. most csak végtelen ciklusba kerülünk tőle.

        // FORMATTED TEXT
        r.registerForContextType(DOMPeerCreationRequest.class, OrderedList.class, DOMOrderedListPeer::new);

        // CONTROL
        r.registerForContextType(DOMPeerCreationRequest.class, PlainTextEditor.class, DOMEditableTextPeer::new);
        //r.addPeerIndependent(DOMPeerCreationRequest.class, Button.class, b -> new WidgetStateRequest<>(() -> new ButtonPeer(b, this), cf));
        r.registerForContextType(DOMPeerCreationRequest.class, CheckBox.class, DOMCheckBoxPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, RadioButton.class, DOMRadioButtonPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, ComboBox.class, ComboBoxPeer::new);
        r.registerForContextType(DOMPeerCreationRequest.class, Slider.class, SliderPeer::new);
        // TODO r.addPeerIndependent(DOMPeerCreationRequest.class, StylesheetRef.class, sr -> handleStylesheet(sr), sr));
        r.registerForContextType(DOMPeerCreationRequest.class, Tooltip.class, t -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofTooltipTag(t), t.content()));
    }

    private void addCSSBackgroundResolvers(ResolverRegistry r) {
        // CSS background
        r.registerForContextType(DOMPeerBase.CSSBackgroundImagePeerCreationRequest.class, SVGImageView.class, svg -> {
            if (svg.isInteractive())
                throw new RuntimeException("interactive SVGImageView inside CSSBackgroundImageContext");
            if (!svg.embeddedWidgets().isEmpty())
                throw new RuntimeException("embedded widgets in SVG inside CSSBackgroundImageContext");
            return new DOMCoverPeer.CSSBackgroundImage(svg.source().toURI());
        });
        r.registerForContextType(DOMPeerBase.CSSBackgroundImagePeerCreationRequest.class, SVGImageView.class,
                jpg -> new DOMCoverPeer.CSSBackgroundImage(jpg.source().toURI()));
    }
}
