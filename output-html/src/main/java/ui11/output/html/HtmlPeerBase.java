package ui11.output.html;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.color.Color;
import ui11.output.html.util.UTF8Writer;
import ui11.provide.Provider;
import ui11.text.TextStyle;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

public abstract class HtmlPeerBase extends Widget {

    @Inject private CSSClassList cssClassListObservable;
    @Inject private LastAppliedTextStyle lastAppliedTextStyle;
    @Inject private TextStyle textStyle;

    protected @NonNull Expose<WriteableHtmlElement> htmlElement(BiConsumer<UTF8Writer, HTMLElementRequirements> writeFunction) {
        return new Expose<>(HtmlPeerCreationRequest.INSTANCE,
            new WriteableHtmlElement(
                cssClassListObservable.classes(),
                textStyle.diffTo(lastAppliedTextStyle.textStyle()),
                writeFunction,
                this::asColor));
    }

    protected final Widget makePeer(Widget widget, Function<WriteableHtmlElement, Widget> f) {
        widget = wrapChild(widget);
        return ExposeRequest.requestSingle(widget, HtmlPeerCreationRequest.INSTANCE, f);
    }

    protected Widget makePeers(List<? extends Widget> widgets,
                               Function<List<WriteableHtmlElement>, Widget> f) {
        return ExposeRequest.requestOnMultipleWidgets(
            widgets.stream().map(this::wrapChild).toList(),
            HtmlPeerCreationRequest.INSTANCE,
            f
        );
    }

    protected final Widget makePeers(List<? extends Widget> widgets,
                                     Set<ExposeRequest<?>> additionalRequests,
                                     BiFunction<List<WriteableHtmlElement>, Map<ExposeRequest<?>, ? extends List<?>>,
                                         Widget> f) {

        Set<ExposeRequest<?>> requests = new HashSet<>(additionalRequests);
        requests.add(HtmlPeerCreationRequest.INSTANCE);
        return ExposeRequest.requestMultiple(
            widgets.stream().map(this::wrapChild).toList(),
            requests,
            results ->
                f.apply((List<WriteableHtmlElement>) results.get(HtmlPeerCreationRequest.INSTANCE), results)
        );
    }

    protected <K> Widget makePeers(Map<@NonNull K, ? extends @NonNull Widget> widgets,
                                   Function<Map<K, ? extends WriteableHtmlElement>, Widget> f) {
        Map<K, Widget> map = widgets.entrySet().stream().
            collect(toMap(Map.Entry::getKey, e -> wrapChild(e.getValue())));
        return ExposeRequest.requestOnMultipleWidgets(map, HtmlPeerCreationRequest.INSTANCE, f::apply);
    }

    private @NonNull Widget wrapChild(@NonNull Widget widget) {
        widget = new Provider<>(LastAppliedTextStyle.class, new LastAppliedTextStyle(textStyle), widget);
        widget = new Provider<>(CSSClassList.class, CSSClassList.CLEAR, widget);
        return widget;
    }

    protected @Nullable Color asColor() {
        return null;
    }

    record LastAppliedTextStyle(TextStyle textStyle) {}

    public static final class HtmlPeerCreationRequest extends ExposeRequest<WriteableHtmlElement> {

        public static final HtmlPeerCreationRequest INSTANCE = new HtmlPeerCreationRequest();

        private HtmlPeerCreationRequest() {
            super(WriteableHtmlElement.class);
        }
    }
}
