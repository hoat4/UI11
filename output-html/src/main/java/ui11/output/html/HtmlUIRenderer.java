package ui11.output.html;

import ui11.ExposeRequest;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.WidgetTree;
import ui11.color.Color;
import ui11.output.html.HTMLElementRequirements.StyleProp;
import ui11.output.html.HtmlPeerBase.LastAppliedTextStyle;
import ui11.output.html.util.UTF8Writer;
import ui11.provide.Provider;
import ui11.text.TextAlign;
import ui11.text.TextStyle;
import ui11.text.TextStyle.FontWeight;

import java.util.concurrent.Executor;

public class HtmlUIRenderer {

    // TODO default wrapping DOM-nál 12. miért van ez a különbség?
    private static final TextStyle defaultTextStyle = new TextStyle(Color.BLACK, 14D,
        "sans-serif", TextAlign.LEFT, FontWeight.NORMAL, TextStyle.Wrapping.NEVER, false,
        null, null, null);

    private final Widget content;
    private final UTF8Writer out;
    private final HTMLElementRequirements layoutReq;

    private HtmlUIRenderer(Widget content, UTF8Writer out, HTMLElementRequirements layoutReq) {
        this.content = content;
        this.out = out;
        this.layoutReq = layoutReq;
    }

    public static void renderAsPage(Widget object, UTF8Writer out) {
        out.write("<!DOCTYPE html>\r\n");
        out.write("<meta charset=\"utf-8\"><link href=\"");
        HTMLOutputUtil.attrValue(out, "../platform-dom/src/main/resources/ui11/platform/dom/layout.css");
        out.write("\" rel=stylesheet>\r\n");

        renderAsBody(object, out);
    }

    public static void renderAsBody(Widget object, UTF8Writer out) {
        HTMLElementRequirements req = new HTMLElementRequirements();
        req.type = HTMLElementType.BODY;
        new HtmlUIRenderer(object, out, req).start();
    }

    public static void renderAsFragment(Widget object, UTF8Writer out, String layoutCSS) {
        HTMLElementRequirements req = new HTMLElementRequirements();
        req.styleProps = new StyleProp(req.styleProps) {
            @Override
            public void write(UTF8Writer out11) {
                HTMLOutputUtil.attrValue(out11, layoutCSS);
            }
        };

        new HtmlUIRenderer(object, out, req).start();
    }

    private void start() {
        Executor executor = task -> {
            // ignore
        };
        WidgetTree.create(new Widget() {

            @Override
            protected Widget build() {
                Widget o = content;
                o = new Provider<>(TextStyle.class, defaultTextStyle, o);
                o = new Provider<>(LastAppliedTextStyle.class, new LastAppliedTextStyle(defaultTextStyle), o);
                o = new Provider<>(CSSClassList.class, CSSClassList.CLEAR, o);

                // TODO ez a komment érvényes még?
                // TODO ha itt az instantiate nem sikerül, tovább kéne dobni. most valszeg StackOverflowError
                //      lesz belőle, mert WidgetResolvert nem talál a hibaüzenethez.

                return ExposeRequest.requestSingle(o, HtmlPeerBase.HtmlPeerCreationRequest.INSTANCE, peer -> {
                    peer.write(out, layoutReq);
                    return new SubstitutedWidget() {};
                });
            }
        }, executor);
    }
}
