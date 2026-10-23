package ui11.output.html;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.control.PlainTextEditor;
import ui11.control.Table;
import ui11.css.CSSClassTag;
import ui11.decoration.Box;
import ui11.graphics.Empty;
import ui11.graphics.effect.Overlay;
import ui11.graphics.fill.ColorFill;
import ui11.layout.multichild.Grid;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Padding;
import ui11.media.JPEGImageView;
import ui11.media.SVGImageView;
import ui11.output.html.HtmlPeerBase.HtmlPeerCreationRequest;
import ui11.provide.Provider;
import ui11.text.Text;

import java.util.Set;

public class HTMLResolverProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.registerForContextType(HtmlPeerCreationRequest.class, Align.class, HtmlAlignPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, Box.class, HtmlBoxPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, ColorFill.class, HtmlColorFillPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, CSSClassTag.class,
            c -> new Provider<>(CSSClassList.class, new CSSClassList(Set.copyOf(c.classNames())), c.content()));
        //r.registerForContextType(HtmlPeerCreationRequest.class, ClientSideElementWrapper.class,
        //    cs -> new WidgetStateRequest<>(HtmlClientSideElementWrapperPeer::new, cs));
        r.registerForContextType(HtmlPeerCreationRequest.class, Empty.class,
            e -> HtmlEmptyPeer.INSTANCE);
        r.registerForContextType(HtmlPeerCreationRequest.class, Grid.class, HtmlGridPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, LinearLayout.class, HtmlLinearLayoutPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, JPEGImageView.class,
            jpegImageView -> new HtmlURLImageView(jpegImageView.source().toURIString()));
        r.registerForContextType(HtmlPeerCreationRequest.class, Overlay.class, HtmlOverlayLayoutPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, Padding.class, HtmlPaddingPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, PlainTextEditor.class, HtmlEditableTextPeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, SVGImageView.class, svg -> {
            if (svg.isInteractive() || !svg.embeddedWidgets().isEmpty())
                throw new RuntimeException("TODO " + svg);
            return new HtmlURLImageView(svg.source().toURIString());
        });
        r.registerForContextType(HtmlPeerCreationRequest.class, Table.class, HtmlTablePeer::new);
        r.registerForContextType(HtmlPeerCreationRequest.class, Text.class, HtmlTextPeer::new);
    }
}
