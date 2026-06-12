package ui11;

import ui11.observable.Observable;
import ui11.provide.Provider;
import ui11.resolution.WidgetResolver;

import java.util.Objects;

final class WidgetResolverProvider extends Widget {

    private final Widget content;
    private final WidgetResolver resolver;

    @Inject private WidgetResolver prev;

    WidgetResolverProvider(Widget content, WidgetResolver resolver) {
        this.content = Objects.requireNonNull(content);
        this.resolver = Objects.requireNonNull(resolver);
    }

    @Override
    protected Widget build() {
        return new Provider<>(WidgetResolver.class, WidgetResolver.composite(prev, resolver), content);
    }
}
