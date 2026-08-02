package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Tartalmat biztosít az általa ismert típusú elemekhez. Megvalósítja egyrészt minden renderer, másrészt a jövőben
 * megvalósíthatják majd "look and feel"/"theme"-szerű modulok.
 */
public abstract class WidgetResolver {

    protected abstract @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget);

    protected abstract @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget,
                                                                  PeerRequestor.@NonNull Request<?> request);

    public static WidgetResolver composite(@NonNull WidgetResolver defaults,
                                           @NonNull WidgetResolver override) {
        Objects.requireNonNull(defaults, "WRc d");
        Objects.requireNonNull(override, "WRc o");
        return new CompositeWidgetResolver(List.of(override, defaults));
    }

    static class CompositeWidgetResolver extends WidgetResolver {

        final List<? extends WidgetResolver> resolvers;

        public CompositeWidgetResolver(List<? extends WidgetResolver> resolvers) {
            this.resolvers = List.copyOf(resolvers);
        }

        @Override
        protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget, PeerRequestor.@NonNull Request<?> request) {
            throw new UnsupportedOperationException();
        }

        Iterable<? extends WidgetResolver> leaves() {
            return () -> {
                Deque<Iterator<? extends WidgetResolver>> q = new LinkedList<>();
                Set<WidgetResolver.CompositeWidgetResolver> visited = Collections.newSetFromMap(new IdentityHashMap<>());

                visited.add(this);
                q.push(this.resolvers.iterator());

                return new Iterator<>() {

                    private WidgetResolver next = findNext();

                    private WidgetResolver findNext() {
                        while (!q.isEmpty()) {
                            Iterator<? extends WidgetResolver> iterator = q.element();
                            if (iterator.hasNext()) {
                                WidgetResolver r = iterator.next();
                                assert r != null;
                                if (r instanceof WidgetResolver.CompositeWidgetResolver c) {
                                    if (visited.add(c))
                                        q.push(c.resolvers.iterator());
                                } else {
                                    return r;
                                }
                            } else {
                                q.pop();
                            }
                        }
                        return null;
                    }

                    @Override
                    public boolean hasNext() {
                        return next != null;
                    }

                    @Override
                    public WidgetResolver next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        WidgetResolver r = next;
                        next = findNext();
                        return r;
                    }
                };
            };
        }
    }
}
