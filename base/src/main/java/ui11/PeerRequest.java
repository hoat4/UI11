package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * Describes what information to query about a widget (or multiple widgets).
 * For example, on a layout query, the Request object would contain the size constraints,
 * and the result will be the computed size.
 * <p>
 * If two requests are equal according to {@link Object#equals(Object)}, then they are performed only once.
 */
public abstract class PeerRequest<P> {

    private final Class<P> peerType;

    protected PeerRequest(Class<P> peerType) {
        this.peerType = peerType;
    }

    // TODO van valami haszna, hogy ez publikus?
    public final Class<P> peerType() {
        return peerType;
    }

    /**
     * Ha nem {@code null}, akkor van defaultja, ekkor az első {@linkplain SubstitutedWidget SubstitutedWidgetnél}
     * meg fog állni a keresése. Ha {@code null}, akkor nincs defaultja
     */
    // TODO cache value of this
    protected @Nullable P defaultValue() {
        return null;
    }

    public final Widget createResponse(@NonNull P peer) {
        Objects.requireNonNull(peer);
        return new PeerRequestor.ResponseWidget<>(this, peer, null);
    }

    public final Widget createResponse(@NonNull P peer, @NonNull Widget chainedWidget) {
        Objects.requireNonNull(peer);
        return new PeerRequestor.ResponseWidget<>(this, peer, chainedWidget);
    }

    @NullMarked
    public static <P> Widget requestSingle(Widget widget,
                                           PeerRequest<P> request,
                                           Function<P, Widget> then) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(request);
        Objects.requireNonNull(then);
        return new PeerRequestor.CreatePeerForSingle<>(widget, request, then, null);
    }

    @NullMarked
    static <P> Widget requestSingle_inheritOtherReqs(Widget widget,
                                                     PeerRequest<P> request,
                                                     ResolutionRequestCollection inheritedReqs,
                                                     Function<P, Widget> then) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(request);
        Objects.requireNonNull(then);
        return new PeerRequestor.CreatePeerForSingle<>(widget, request, then, inheritedReqs);
    }

    // TODO REQ extends Request<P> nem lehet, mert akkor egy Set<Request<?>>-vel nem lehet meghívni ezt a függvényt
    // TODO most hogy Result osztály már nincs, RES típusváltozóra még szükség van ebben a formában?
    @NullMarked
    public static <REQ extends PeerRequest<?>, RES> Widget requestOnSingleWidget(Widget widget,
                                                                                 Set<REQ> requests,
                                                                                 Function<Map<REQ, RES>, Widget> then) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(requests);
        Objects.requireNonNull(then);

        enum Single {SINGLE}
        return requestMultiple(
                Map.of(Single.SINGLE, widget),
                Map.of(Single.SINGLE, Set.copyOf(requests)),
                (Map<PeerRequest<?>, Map<Single, ?>> results) -> {
                    Map<PeerRequest<?>, ?> results2 = results.entrySet().stream().collect(toUnmodifiableMap(
                            Map.Entry::getKey, (Map.Entry<PeerRequest<?>, Map<Single, ?>> entry) -> {
                                Map<Single, ?> m = entry.getValue();
                                Object result = m.get(Single.SINGLE);
                                assert result != null;
                                return result;
                            }));
                    @SuppressWarnings("unchecked") Map<REQ, RES> castedMap = (Map<REQ, RES>) results2;
                    return then.apply(castedMap);
                });
    }

    // azért nem ofMultiple, mert az arra utalna, hogy több req is van, nem csak több widget
    @NullMarked
    public static <P> Widget requestOnMultipleWidgets(List<? extends Widget> widgets,
                                                      PeerRequest<P> request,
                                                      Function<List<P>, Widget> then) {
        widgets = List.copyOf(widgets);
        return new PeerRequestor.CreatePeersForList<>(widgets, Collections.nCopies(widgets.size(), request), then);
    }

    @NullMarked
    public static <K, P> Widget requestOnMultipleWidgets(Map<K, ? extends Widget> widgets,
                                                         PeerRequest<P> request,
                                                         Function<Map<K, P>, Widget> then) {
        // most ez a Map.copyOf NPE-t akkor is, ha K-k között van egy null.
        // ha mégis kell null K, akkor kézzel kell ellenőrizni
        // (de akkor ofMultiple-t is módosítsuk eszerint)
        // TODO kéne írni OpenJDK-nak, hogy Collectors.toMap null value-knál exceptiont dob
        widgets = Map.copyOf(widgets);
        Objects.requireNonNull(request);
        Objects.requireNonNull(then);

        Map<K, ? extends Set<PeerRequest<P>>> requests = widgets.keySet().stream().
                collect(toMap(k -> k, k -> Set.of(request)));
        return new PeerRequestor.CreatePeersForMap<>(
                widgets,
                requests,
                results -> {
                    Map<K, P> map = results.get(request);
                    assert map != null;
                    return then.apply(map);
                }
        );
    }

    @NullMarked
    public static <P> Widget requestMultiple(
            List<? extends Widget> widgets,
            List<? extends PeerRequest<P>> requests,
            Function<List<P>, Widget> then) {
        widgets = List.copyOf(widgets);
        requests = List.copyOf(requests);
        Objects.requireNonNull(then);
        if (widgets.size() != requests.size())
            throw new IllegalArgumentException();

        return new PeerRequestor.CreatePeersForList<>(widgets, requests, then);
    }

    @NullMarked
    public static Widget requestMultiple(
            List<? extends Widget> widgets,
            Set<PeerRequest<?>> requests,
            Function<Map<PeerRequest<?>, ? extends List<?>>, Widget> then) {
        List<? extends Widget> widgets2 = List.copyOf(widgets);
        Set<PeerRequest<?>> requests2 = Set.copyOf(requests);
        Objects.requireNonNull(then);

        // TODO CreatePeersForList nem tud több requestet, de ez a mapes izé meg lassú
        Map<Integer, Widget> widgetsMap = IntStream.range(0, widgets2.size()).
                boxed().collect(toUnmodifiableMap(i -> i, widgets2::get));
        Map<Integer, Set<PeerRequest<?>>> requestsMap = IntStream.range(0, widgets2.size()).
                boxed().collect(toUnmodifiableMap(i -> i, i -> requests2));
        return requestMultiple(widgetsMap, requestsMap, resultMap -> {
            assert resultMap.keySet().equals(requests2);
            Map<PeerRequest<?>, List<Object>> lists = new HashMap<>();
            resultMap.forEach((req, resultsForReq) -> {
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < widgets2.size(); i++) {
                    list.add(resultsForReq.get(i));
                }
                lists.put(req, list);
            });
            return then.apply(lists);
        });
    }

    @NullMarked
    public static <K> Widget requestMultiple(Map<K, ? extends Widget> widgets,
                                             Map<K, Set<PeerRequest<?>>> requests,
                                             Function<Map<PeerRequest<?>, Map<K, ?>>, Widget> then) {
        // most ez a Map.copyOf NPE-t akkor is, ha K-k között van egy null.
        // ha mégis kell null K, akkor kézzel kell ellenőrizni
        // (de akkor ofMultiple-t is módosítsuk eszerint)
        // TODO kéne írni OpenJDK-nak, hogy Collectors.toMap null value-knál exceptiont dob
        widgets = Map.copyOf(widgets);
        requests = requests.entrySet().stream().collect(toUnmodifiableMap(
                Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
        @SuppressWarnings("unchecked") Map<K, Set<PeerRequest<Object>>> requests2 = (Map<K, Set<PeerRequest<Object>>>) (Map<K, ?>) requests;
        @SuppressWarnings("unchecked") Function<Map<PeerRequest<Object>, Map<K, Object>>, Widget> then2 =
                (Function<Map<PeerRequest<Object>, Map<K, Object>>, Widget>) (Function<?, Widget>) then;
        return new PeerRequestor.CreatePeersForMap<>(widgets, requests2, then2);
    }

    // TODO név?
    @NullMarked
    public static <P> Collector<Widget, ?, Widget> requestingCollector(
            PeerRequest<P> request,
            Function<Stream<P>, Widget> then) {
        // then-ben azért Stream van List helyett, mert így majd lehetne
        // olyat csinálni, hogy mondjuk ne resolveolja a végén lévő
        // widgeteket feleslegesen, ha azok resultjait úgyse olvassuk ki.
        // mondjuk ez félig értelmetlenné vált most hogy átálltunk iteratív tree refreshgre.
        return Collectors.collectingAndThen(Collectors.toList(),
                list -> {
                    list = List.copyOf(list);
                    return new PeerRequestor.CreatePeersForList<>(
                            list,
                            Collections.nCopies(list.size(), request),
                            l -> then.apply(l.stream())
                    );
                });
    }
}
