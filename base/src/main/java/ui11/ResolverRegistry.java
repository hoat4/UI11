package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ResolverRegistry {

    private final List<PeerIndependentEntry<?>> peerIndependentEntries = new ArrayList<>();
    private final List<PeerDependentEntry<?, ?>> peerDependentEntries = new ArrayList<>();

    ResolverRegistry() {
    }

    public <SW extends SubstitutedWidget> void addPeerIndependent(
            @Nullable Class<? extends PeerRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        if (requestType != null)
            validateRequestType(requestType);
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        peerIndependentEntries.add(new PeerIndependentEntry<>(requestType, widgetType, f));
    }

    public <SW extends SubstitutedWidget, REQ extends PeerRequest<?>> void addPeerDependent(
            @NonNull Class<REQ> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull BiFunction<@NonNull SW, @NonNull REQ, @NonNull Widget> f) {
        validateRequestType(requestType);
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        peerDependentEntries.add(new PeerDependentEntry<>(requestType, widgetType, f));
    }

    public <REQ extends PeerRequest<?>> void addPeerDependent(
            @NonNull Class<REQ> requestType,
            @NonNull Set<Class<? extends SubstitutedWidget>> widgetTypes,
            @NonNull Function<REQ, Widget> f) {
        // majd később talán lehet használni optimalizálásokra azt az infót, hogy ezeknek a widgettypeoknak
        // ugyanaz a resolverjük és nem is függnek a widgettől, tehát összevonhatóak.
        // De mivel egyelőre nincsenek optimalizálások, ezért csak egymástól függetlenül bejegyezzük.

        widgetTypes = Set.copyOf(widgetTypes);

        if (widgetTypes.isEmpty()) {
            validateRequestType(requestType);
            Objects.requireNonNull(f);
        } else {
            for (Class<? extends SubstitutedWidget> widgetType : widgetTypes)
                addPeerDependent(requestType, widgetType, (w, req) -> f.apply(req));
        }
    }

    private static void validateRequestType(Class<? extends PeerRequest<?>> requestType) {
        Objects.requireNonNull(requestType);
        requestType.asSubclass(PeerRequest.class);
        // TODO IntelliJ bug, enélkül a cast nélkül nem jelzi hibának
        if (requestType == (Class<? extends PeerRequest<?>>) (Class<?>) PeerRequest.class)
            throw new IllegalArgumentException();
    }

    private static void validateSubstitutedWidgetType(Class<? extends SubstitutedWidget> substitutedWidgetType) {
        Objects.requireNonNull(substitutedWidgetType);
        substitutedWidgetType.asSubclass(SubstitutedWidget.class);
        if (substitutedWidgetType == SubstitutedWidget.class)
            throw new IllegalArgumentException();
    }

    void findPeerDependentResolvers(
            SubstitutedWidget w,
            PeerRequest<?> req,
            Consumer<BiFunction<? extends SubstitutedWidget, ? extends PeerRequest<?>, Widget>> consumer) {
        for (PeerDependentEntry<?, ?> e : peerDependentEntries) {
            if (!e.widgetType.isInstance(w))
                continue;
            if (!e.requestType.isInstance(req))
                continue;
            consumer.accept(e.f);
        }
    }

    void findPeerIndependentResolvers(
            SubstitutedWidget w,
            Set<? extends PeerRequest<?>> reqs,
            BiConsumer<Object /* key */, Function<? extends SubstitutedWidget, Widget>> consumer) {
        for (PeerIndependentEntry<?> e : peerIndependentEntries) {
            if (!e.widgetType.isInstance(w))
                continue;
            if (e.requestType != null && reqs.stream().noneMatch(e.requestType::isInstance))
                continue;
            consumer.accept(e, e.f);
        }
    }

    Stream<Entry> all() {
        return Stream.concat(peerDependentEntries.stream(), peerIndependentEntries.stream());
    }

    static sealed abstract class Entry {

        final Class<? extends PeerRequest<?>> requestType; // csak peer independent esetén nullable
        final @NonNull Class<? extends SubstitutedWidget> widgetType;

        public Entry(Class<? extends PeerRequest<?>> requestType,
                     @NonNull Class<? extends SubstitutedWidget> widgetType) {
            this.requestType = requestType;
            this.widgetType = widgetType;
        }
    }

    static final class PeerIndependentEntry<SW extends SubstitutedWidget> extends Entry {

        private final Function<SW, Widget> f;

        public PeerIndependentEntry(@Nullable Class<? extends PeerRequest<?>> requestType,
                                    @NonNull Class<SW> widgetType,
                                    @NonNull Function<@NonNull SW, @NonNull Widget> f) {
            super(requestType, widgetType);
            this.f = f;
        }

        @Override
        public String toString() {
            return ReflectionUtil.simpleName(widgetType) + " " +
                    (requestType != null ? ReflectionUtil.simpleName(requestType) : "<any request>") +
                    " " + f;
        }
    }

    static final class PeerDependentEntry<SW extends SubstitutedWidget, REQ extends PeerRequest<?>> extends Entry {

        private final BiFunction<SW, REQ, Widget> f;

        public PeerDependentEntry(@NonNull Class<REQ> requestType,
                                  @NonNull Class<SW> widgetType,
                                  @NonNull BiFunction<@NonNull SW, @NonNull REQ, @NonNull Widget> f) {
            super(requestType, widgetType);
            this.f = f;
        }

        @Override
        public String toString() {
            return ReflectionUtil.simpleName(widgetType) + " " +
                    ReflectionUtil.simpleName(requestType) +
                    " " + f;
        }
    }

}